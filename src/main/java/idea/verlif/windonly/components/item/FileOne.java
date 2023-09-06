package idea.verlif.windonly.components.item;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.utils.SystemExecUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileOne extends BorderPane implements Item<File> {

    private final File file;
    private final double height;

    public FileOne(File file) {
        this.file = file;
        this.height = WindonlyConfig.getInstance().getFontSize() * 8;
    }

    @Override
    public void init() {
        // 设置宽高样式
        setPrefWidth(height);
        setPrefHeight(height);
        setPadding(ItemInsets.INSETS);

        setCenter(new FileIconImageView(file));
        setBottom(createFilenameNode(file));

        // 设置双击打开
        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                SystemExecUtil.openFileByExplorer(file.getAbsolutePath());
            }
        });
    }

    private Node createFilenameNode(File file) {
        Label label = new Label(file.getName());
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(height);
        label.setFont(new Font(WindonlyConfig.getInstance().getFontSize()));
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

    /**
     * 文件图标样式
     */
    private static final class FileIconImageView extends ImageView {

        private static final String[] PICTURE_SUFFIX = {"png", "jpg", "jpeg", "bmp", "gif"};

        public FileIconImageView(File file) {
            super();
            Image image = null;
            String filename = file.getName().toLowerCase();
            for (String suffix : PICTURE_SUFFIX) {
                if (filename.endsWith(suffix)) {
                    image = new Image(file.getAbsolutePath());
                }
            }
            // 默认图片
            if (image == null) {
                try (InputStream iconStream = getClass().getResourceAsStream(file.isFile() ? "/images/file.png" : "/images/directory.png")) {
                    if (iconStream != null) {
                        image = new Image(iconStream);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (image != null) {
                setImage(image);
            }

            setFitWidth(WindonlyConfig.getInstance().getImageSize());
            setFitHeight(WindonlyConfig.getInstance().getImageSize());
        }
    }

    private static final class ItemInsets extends Insets {

        public static final ItemInsets INSETS = new ItemInsets();

        public ItemInsets() {
            super(4 * WindonlyConfig.getInstance().getMagnification(),
                    4 * WindonlyConfig.getInstance().getMagnification(),
                    4 * WindonlyConfig.getInstance().getMagnification(),
                    4 * WindonlyConfig.getInstance().getMagnification());
        }
    }
}
