package idea.verlif.windonly.components.item;

import idea.verlif.easy.file.util.FileUtil;
import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.stage.ImagePreviewer;
import idea.verlif.windonly.stage.TextPreviewer;
import idea.verlif.windonly.utils.FileTypeUtil;
import idea.verlif.windonly.utils.ImageUtil;
import idea.verlif.windonly.utils.SystemExecUtil;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileOne extends BorderPane implements Item<File> {

    /**
     * 系统图标缓存：按扩展名缓存。
     * <p>
     * 原实现每次刷新都会调用 {@code FileSystemView.getSystemIcon(file)}，
     * 这是走系统 Shell 的重操作，列表一刷新就会卡顿，而且每次都会生成新的
     * BufferedImage 与 JavaFX Image（两者都占用堆外/堆内内存）。
     * 同一种扩展名的图标本质上是一样的，缓存后只会真正解析一次。
     */
    private static final Map<String, Image> EXT_ICON_CACHE = new ConcurrentHashMap<>();

    private static final int ICON_CACHE_LIMIT = 64;

    /**
     * 内置文本预览的文件大小上限，超过则交给系统程序打开
     */
    private static final long MAX_PREVIEW_FILE_SIZE = 2 * 1024 * 1024;

    private final File file;
    /**
     * 是否是横向布局
     */
    private boolean horizontal = true;
    /**
     * 图片尺寸
     */
    private double imageSize = WindonlyConfig.getInstance().getFontSize();

    public FileOne(File file) {
        this.file = file;
    }

    @Override
    public void init() {
        // 设置宽高样式
        setPadding(ItemInsets.INSETS);

        String path = file.getAbsolutePath();
        // 设置文件点击事件
        if (FileTypeUtil.isImage(file)) {
            setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() > 1) {
                    if (mouseEvent.isControlDown()) {
                        SystemExecUtil.openFileByExplorer(path);
                    } else {
                        new ImagePreviewer(path).show();
                    }
                    mouseEvent.consume();
                }
            });
        } else if (FileTypeUtil.isText(file)) {
            setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() > 1) {
                    if (mouseEvent.isControlDown() || file.length() > MAX_PREVIEW_FILE_SIZE) {
                        // 超大文本直接交给系统打开，避免把整个文件读进内存再塞进 TextArea
                        SystemExecUtil.openFileByExplorer(path);
                    } else {
                        String text = FileUtil.readContentAsString(file);
                        new TextPreviewer(text).show();
                    }
                    mouseEvent.consume();
                }
            });
        } else {
            setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() > 1) {
                    SystemExecUtil.openFileByExplorer(path);
                    mouseEvent.consume();
                }
            });
        }
        refresh();
    }

    private Label createFilenameNode(File file) {
        Label label = new Label(file.getName());
        label.setAlignment(Pos.CENTER);
        label.setFont(new Font(WindonlyConfig.getInstance().getFontSize() * 0.8));
        // 设置提示
        label.setTooltip(new Tooltip(file.getAbsolutePath()));
        return label;
    }

    @Override
    public File getSource() {
        return file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean match(String key) {
        return file.getAbsolutePath().contains(key);
    }

    @Override
    public boolean sourceEquals(File file) {
        return this.file.equals(file);
    }

    @Override
    public void refresh() {
        // 设置文件图标与提示文本
        FileIconImageView iconView = new FileIconImageView(file);
        Label nameNode = createFilenameNode(file);
        if (horizontal) {
            // 横向显示
            setLeft(iconView);
            setCenter(nameNode);
            setAlignment(nameNode, Pos.CENTER_LEFT);
            setMargin(nameNode, new Insets(0, 0, 0, 8));
        } else {
            // 纵向显示
            setCenter(iconView);
            setBottom(nameNode);
            setAlignment(nameNode, Pos.BOTTOM_LEFT);
        }
        setAlignment(iconView, Pos.CENTER_LEFT);
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }

    public double getImageSize() {
        return imageSize;
    }

    public void setImageSize(double imageSize) {
        this.imageSize = imageSize;
    }

    /**
     * 文件图标样式
     */
    private final class FileIconImageView extends ImageView {

        public FileIconImageView(File file) {
            super();
            Image image = null;
            long maxSize = WindonlyConfig.getInstance().getDisplayImageMaxSize();
            if (FileTypeUtil.isImage(file) && (maxSize < 0 || file.length() < maxSize)) {
                // 按显示尺寸解码，而不是把整张原图读进内存
                try {
                    image = ImageUtil.loadForDisplay(file.getAbsolutePath(), imageSize);
                } catch (Throwable ignored) {
                    image = null;
                }
            }
            // 默认图片
            if (image == null) {
                image = getFileImage(file);
            }
            if (image != null) {
                double imageHeight = image.getHeight();
                if (imageHeight > imageSize) {
                    // 等比缩小
                    setFitWidth(imageSize / imageHeight * image.getWidth());
                    setFitHeight(imageSize);
                } else {
                    // 等比缩小
                    setFitWidth(image.getWidth());
                    setFitHeight(imageHeight);
                }
                setImage(image);
            }
        }
    }

    /**
     * 文件图标样式（静态资源与系统图标加载）
     */
    private static final class FileIcons {

        private static final Image DIRECTORY_ICON;
        private static final Image FILE_ICON;

        static {
            Image directory = null;
            Image file = null;
            try (InputStream dirStream = FileIcons.class.getResourceAsStream("/images/file/directory.png");
                 InputStream fileStream = FileIcons.class.getResourceAsStream("/images/file/file.png")) {
                if (dirStream != null) {
                    directory = new Image(dirStream);
                }
                if (fileStream != null) {
                    file = new Image(fileStream);
                }
            } catch (IOException e) {
                throw new WindonlyException(e);
            }
            DIRECTORY_ICON = directory;
            FILE_ICON = file;
        }
    }

    /**
     * 获取文件对应的资源图片
     */
    private static Image getFileImage(File file) {
        if (file.isDirectory()) {
            return FileIcons.DIRECTORY_ICON;
        }
        String extension = extensionOf(file);
        Image cached = EXT_ICON_CACHE.get(extension);
        if (cached != null) {
            return cached;
        }
        Image image = loadSystemIcon(file);
        if (image == null) {
            image = FileIcons.FILE_ICON;
        }
        if (EXT_ICON_CACHE.size() >= ICON_CACHE_LIMIT) {
            EXT_ICON_CACHE.clear();
        }
        EXT_ICON_CACHE.put(extension, image);
        return image;
    }

    private static String extensionOf(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static Image loadSystemIcon(File file) {
        try {
            Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
            if (!(icon instanceof ImageIcon imageIcon)) {
                return null;
            }
            java.awt.Image image = imageIcon.getImage();
            if (image == null) {
                return null;
            }
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            if (width <= 0 || height <= 0) {
                return null;
            }
            // 将 AWT 图像转换为 BufferedImage
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            // 将 BufferedImage 转换为 JavaFX 的 Image
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Throwable t) {
            // 图标获取失败不应该影响列表展示
            return null;
        }
    }

    private static final class ItemInsets extends Insets {

        public static final ItemInsets INSETS = new ItemInsets();

        public ItemInsets() {
            super(3 * WindonlyConfig.getInstance().getMagnification(),
                    3 * WindonlyConfig.getInstance().getMagnification(),
                    3 * WindonlyConfig.getInstance().getMagnification(),
                    3 * WindonlyConfig.getInstance().getMagnification());
        }
    }
}
