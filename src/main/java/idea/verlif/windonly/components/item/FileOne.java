package idea.verlif.windonly.components.item;

import idea.verlif.easy.file.util.FileUtil;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.stage.ImagePreviewer;
import idea.verlif.windonly.stage.TextPreviewer;
import idea.verlif.windonly.utils.FileTypeUtil;
import idea.verlif.windonly.utils.SystemExecUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.embed.swing.SwingFXUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public class FileOne extends BorderPane implements Item<File> {

    private final File file;

    public FileOne(File file) {
        this.file = file;
    }

    @Override
    public void init() {
        // 设置宽高样式
        setPadding(ItemInsets.INSETS);

        // 设置文件点击事件
        if (FileTypeUtil.isImage(file)) {
            setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() > 1) {
                    if (mouseEvent.isControlDown()) {
                        SystemExecUtil.openFileByExplorer(file.getAbsolutePath());
                    } else {
                        new ImagePreviewer(file.getAbsolutePath()).show();
                    }
                }
            });
        } else if (FileTypeUtil.isText(file)) {
            setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() > 1) {
                    if (mouseEvent.isControlDown()) {
                        SystemExecUtil.openFileByExplorer(file.getAbsolutePath());
                    } else {
                        String text = FileUtil.readContentAsString(file);
                        new TextPreviewer(text).show();
                    }
                }
            });
        } else {
            setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() > 1) {
                    SystemExecUtil.openFileByExplorer(file.getAbsolutePath());
                }
            });
        }
        refresh();
    }

    private Node createFilenameNode(File file) {
        Label label = new Label(file.getName());
        label.setAlignment(Pos.CENTER);
        label.setFont(new Font(WindonlyConfig.getInstance().getCalcFontSize() * 0.8));
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
        setLeft(iconView);
        Node nameNode = createFilenameNode(file);
        setCenter(nameNode);
        setMargin(nameNode, new Insets(0, 0, 0, 4));
        setAlignment(nameNode, Pos.CENTER_LEFT);
        setAlignment(iconView, Pos.CENTER_LEFT);
    }

    /**
     * 文件图标样式
     */
    private static final class FileIconImageView extends ImageView {

        public FileIconImageView(File file) {
            super();
            Image image = null;
            // TODO: 过大的图片会导致浏览卡顿
            if (FileTypeUtil.isImage(file) && file.length() < 1024 * 1024) {
                image = new Image(file.getAbsolutePath());
            }
            // 默认图片
            if (image == null) {
                image = getFileImage(file);
            }
            if (image != null) {
                double imageHeight = image.getHeight();
                double imageSize = WindonlyConfig.getInstance().getImageSize();
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
                InputStream stream = getClass().getResourceAsStream("/images/file/directory.png");
                if (stream == null) {
                    stream = getClass().getResourceAsStream("/images/file/file.png");
                }
                if (stream != null) {
                    return new Image(stream);
                } else {
                    return null;
                }
            } else {
                ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
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
