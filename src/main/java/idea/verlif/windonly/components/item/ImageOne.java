package idea.verlif.windonly.components.item;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.stage.ImagePreviewer;
import idea.verlif.windonly.utils.ImageUtil;
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

    /**
     * 列表展示用的图片（可能是按显示尺寸解码后的缩略图）
     */
    private final Image image;
    /**
     * 原始地址，用于在需要完整分辨率时重新加载
     */
    private final String sourceUrl;
    /**
     * 展示图是否已经降采样。降采样后复制/预览需要按原图重新加载，
     * 否则会把缩略图复制到系统剪贴板。
     */
    private final boolean downscaled;

    public ImageOne(Image image) {
        this(image, image == null ? null : image.getUrl(), false);
    }

    private ImageOne(Image image, String sourceUrl, boolean downscaled) {
        this.image = image;
        this.sourceUrl = sourceUrl;
        this.downscaled = downscaled;
    }

    /**
     * 从地址加载只用于列表展示的缩略图。
     * <p>
     * 列表里图片只显示几十像素高，如果直接 {@code new Image(url)}，
     * JavaFX 会把原图完整解码进内存（一张 4000x3000 的照片约 45MB），
     * 多个图片项就足以让内存占用飙升。这里按显示尺寸解码。
     * <p>
     * 解码失败（文件临时不可用等）时仍然保留数据项，避免条目被直接从存档里丢掉。
     */
    public static ImageOne ofUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        Image display = null;
        try {
            display = ImageUtil.loadForDisplay(url, WindonlyConfig.getInstance().getImageSize());
        } catch (Throwable ignored) {
        }
        if (display != null && !display.isError()) {
            return new ImageOne(display, url, true);
        }
        try {
            return new ImageOne(new Image(url), url, false);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public void init() {
        // 设置宽高样式
        setPadding(ItemInsets.INSETS);
        // 设置双击打开
        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() > 1) {
                if (sourceUrl != null) {
                    new ImagePreviewer(sourceUrl).show();
                } else {
                    new ImagePreviewer(image).show();
                }
                mouseEvent.consume();
            }
        });
        refresh();
    }

    private Label createFilenameNode(Image image) {
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

    /**
     * 完整分辨率的图片。仅在复制到剪贴板、预览等需要画质的场景使用。
     */
    public Image getFullImage() {
        if (!downscaled || sourceUrl == null) {
            return image;
        }
        try {
            Image full = new Image(sourceUrl);
            if (!full.isError()) {
                return full;
            }
        } catch (Throwable ignored) {
        }
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
        String url = image.getUrl();
        // 临时图片使用黑色背景，避免透明看不清
        if (url == null) {
            BorderPane innerPane = new BorderPane();
            innerPane.setStyle("-fx-background-color: black");
            innerPane.setCenter(iconView);
            setCenter(innerPane);
        } else {
            setCenter(iconView);
        }
        Node nameNode = createFilenameNode(image);
        setBottom(nameNode);
        setAlignment(iconView, Pos.CENTER_LEFT);
        setAlignment(nameNode, Pos.CENTER_LEFT);
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
