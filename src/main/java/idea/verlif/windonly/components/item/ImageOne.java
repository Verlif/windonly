package idea.verlif.windonly.components.item;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.stage.ImagePreviewer;
import idea.verlif.windonly.utils.MessageUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ImageOne extends BorderPane implements Item<Image> {

    private final Image image;

    public ImageOne(Image image) {
        this.image = image;
    }

    @Override
    public void init() {
        // 设置宽高样式
        setPadding(ItemInsets.INSETS);
        String url = image.getUrl();
        if (url != null) {
            // 设置双击打开
            setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() > 1) {
                    new ImagePreviewer(image).show();
                }
            });
        }
        refresh();
    }

    private Node createFilenameNode(Image image) {
        Label label;
        if (image.getUrl() == null) {
            label = new Label(MessageUtil.get("tempImage"));
            label.setTooltip(new Tooltip(MessageUtil.get("tempImage")));
            label.setTextFill(Color.rgb(151, 101, 101));
        } else {
            label = new Label(image.getUrl());
            label.setTooltip(new Tooltip(image.getUrl()));
        }
        label.setFont(new Font(WindonlyConfig.getInstance().getFontSize() * 0.8));
        label.setAlignment(Pos.CENTER);
        return label;
    }

    @Override
    public Image getSource() {
        return image;
    }

    @Override
    public boolean match(String key) {
        if (image.getUrl() == null) {
            return false;
        }
        return image.getUrl().contains(key);
    }

    @Override
    public boolean sourceEquals(Image image) {
        return this.image == image || (this.image.getUrl() != null && this.image.getUrl().equals(image.getUrl()));
    }

    @Override
    public void refresh() {
        // 设置文件图标与提示文本
        FileIconImageView iconView = new FileIconImageView(image);
        setCenter(iconView);
        Node nameNode = createFilenameNode(image);
        setBottom(nameNode);
        setAlignment(nameNode, Pos.CENTER);
        setAlignment(iconView, Pos.CENTER_LEFT);
    }

    /**
     * 文件图标样式
     */
    private static final class FileIconImageView extends ImageView {

        public FileIconImageView(Image image) {
            super();
            if (image.getUrl() == null) {
                setStyle("-fx-effect: dropshadow(three-pass-box, rgba(187, 187, 187, 0.8), 0, 0, 0, 0);");
            } else {
                setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 0, 0, 0, 0);");
            }
            double imageHeight = image.getHeight();
            if (imageHeight > WindonlyConfig.getInstance().getImageSize()) {
                // 等比缩小
                setFitWidth(WindonlyConfig.getInstance().getImageSize() / imageHeight * image.getWidth());
                setFitHeight(WindonlyConfig.getInstance().getImageSize());
            } else {
                setFitWidth(image.getWidth());
                setFitHeight(imageHeight);
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
