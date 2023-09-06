package idea.verlif.windonly.components.item;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.utils.SystemExecUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class ImageOne extends BorderPane implements Item<Image> {

    private final Image image;
    private final double height;

    public ImageOne(Image image) {
        this.image = image;
        this.height = WindonlyConfig.getInstance().getFontSize() * 8;
    }

    @Override
    public void init() {
        // 设置宽高样式
        setPrefWidth(height);
        setPrefHeight(height);
        setPadding(ItemInsets.INSETS);

        // 设置文件图标与提示文本
        setCenter(new FileIconImageView(image));
        Node nameNode = createFilenameNode(image);
        setBottom(nameNode);
        setAlignment(nameNode, Pos.CENTER);

        // 设置双击打开
        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                SystemExecUtil.openFileByExplorer(image.getUrl());
            }
        });
    }

    private Node createFilenameNode(Image image) {
        Label label = new Label(image.getUrl());
        label.setAlignment(Pos.CENTER);
        label.setFont(new Font(WindonlyConfig.getInstance().getFontSize()));
        // 设置提示
        label.setTooltip(new Tooltip(image.getUrl()));
        return label;
    }

    @Override
    public Image getSource() {
        return image;
    }

    @Override
    public boolean match(String key) {
        return image.getUrl().contains(key);
    }

    @Override
    public boolean sourceEquals(Image image) {
        return this.image.equals(image);
    }

    /**
     * 文件图标样式
     */
    private static final class FileIconImageView extends ImageView {

        public FileIconImageView(Image image) {
            super();
            double imageHeight = image.getHeight();
            if (imageHeight > WindonlyConfig.getInstance().getImageSize()) {
                // 等比缩小
                setFitWidth(WindonlyConfig.getInstance().getImageSize() / imageHeight * image.getWidth());
                setFitHeight(WindonlyConfig.getInstance().getImageSize());
            }
            setImage(image);
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
