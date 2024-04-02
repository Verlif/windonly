package idea.verlif.windonly.components.item;

import idea.verlif.easy.file.util.FileUtil;
import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.stage.ImagePreviewer;
import idea.verlif.windonly.stage.TextPreviewer;
import idea.verlif.windonly.utils.FileTypeUtil;
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

public class FileOne extends BorderPane implements Item<File> {

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
                    if (mouseEvent.isControlDown()) {
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
            setAlignment(nameNode, Pos.BOTTOM_CENTER);
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

        private static final Image DIRECTORY_ICON;
        private static final Image FILE_ICON;

        static {
            try (InputStream dirStream = FileIconImageView.class.getResourceAsStream("/images/file/directory.png");
                 InputStream fileStream = FileIconImageView.class.getResourceAsStream("/images/file/file.png")) {
                if (dirStream != null) {
                    DIRECTORY_ICON = new Image(dirStream);
                } else {
                    DIRECTORY_ICON = null;
                }
                if (fileStream != null) {
                    FILE_ICON = new Image(fileStream);
                } else {
                    FILE_ICON = null;
                }
            } catch (IOException e) {
                throw new WindonlyException(e);
            }
        }

        public FileIconImageView(File file) {
            super();
            Image image = null;
            long maxSize = WindonlyConfig.getInstance().getDisplayImageMaxSize();
            if (FileTypeUtil.isImage(file) && (maxSize < 0 || file.length() < maxSize)) {
                image = new Image(file.getAbsolutePath());
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

        /**
         * 获取文件对应的资源图片
         */
        private Image getFileImage(File file) {
            if (file.isDirectory()) {
                return DIRECTORY_ICON;
            } else {
                ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
                if (icon == null) {
                    return FILE_ICON;
                }
                java.awt.Image image = icon.getImage();

                // 将 AWT 图像转换为 BufferedImage
                BufferedImage bufferedImage = new BufferedImage(
                        image.getWidth(null),
                        image.getHeight(null),
                        BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D g2d = bufferedImage.createGraphics();
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
                // 将 BufferedImage 转换为 JavaFX 的 Image
                return SwingFXUtils.toFXImage(bufferedImage, null);
            }
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
