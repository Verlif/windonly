package idea.verlif.windonly.stage;

import idea.verlif.windonly.utils.ImageUtil;
import idea.verlif.windonly.utils.ScreenUtil;
import javafx.animation.ScaleTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

public class ImagePreviewer extends BaseStage {

    /**
     * 放大倍率
     */
    private double magnification = 1.0;
    /**
     * 非透明度
     */
    private double alpha = 1;

    private double startX;
    private double imageStartX;
    private double startY;
    private double imageStartY;

    private final ScaleTransition scaleTransition;

    public ImagePreviewer(String url) {
        this(loadPreviewImage(url));
    }

    public ImagePreviewer(Image image) {
        super();
        BorderPane borderPane = getBorderPane();
        ImageView imageView = new ImageView(image);
        String url = image.getUrl();
        // 临时图片使用黑色背景，避免透明看不清
        if (url == null) {
            borderPane.setStyle("-fx-background-color: black");
        }
        borderPane.setCenter(imageView);
        // 设置初始化大小
        double[] screenSize = ScreenUtil.getScreenSize(this);
        if (screenSize[0] / 2 > image.getWidth() && screenSize[1] / 2 > image.getHeight()) {
            setImageSize(imageView, image.getWidth(), image.getHeight());
        } else {
            setImageSize(imageView, screenSize[0] / 2, screenSize[1] / 2);
        }
        // 设置最小宽度
        setMinWidth(200);
        setMinHeight(100);

        // 滚动缩放的动画复用同一个实例，原实现每次滚动都新建一个 ScaleTransition
        this.scaleTransition = new ScaleTransition(Duration.millis(200), imageView);

        // 设置自适应
        widthProperty().addListener((observableValue, oldWidth, newWidth) -> {
            setImageSize(imageView, newWidth.doubleValue(), borderPane.getHeight());
        });
        heightProperty().addListener((observableValue, oldHeight, newHeight) -> {
            setImageSize(imageView, borderPane.getWidth(), newHeight.doubleValue());
        });
        maximizedProperty().addListener((observableValue, max, t1) -> {
            setImageSize(imageView, borderPane.getWidth(), borderPane.getHeight());
        });
        // 设置鼠标滚轮滚动缩放
        borderPane.setOnScroll(scrollEvent -> {
            double step;
            if (scrollEvent.isControlDown()) {
                step = scrollEvent.getDeltaY() > 0 ? 0.05 : -0.05;
            } else {
                step = scrollEvent.getDeltaY() > 0 ? 0.2 : -0.2;
            }
            double temp = magnification + step;
            magnification = Math.max(temp, 0.1);
            scaleTransition.stop();
            scaleTransition.setToX(magnification);
            scaleTransition.setToY(magnification);
            scaleTransition.playFromStart();
        });
        // 拖拽
        borderPane.setOnMousePressed(mouseEvent -> {
            startX = mouseEvent.getX();
            imageStartX = imageView.getTranslateX();
            startY = mouseEvent.getY();
            imageStartY = imageView.getTranslateY();
        });
        borderPane.setOnMouseDragged(mouseDragEvent -> {
            imageView.setTranslateX(imageStartX + mouseDragEvent.getX() - startX);
            imageView.setTranslateY(imageStartY + mouseDragEvent.getY() - startY);
        });
        // 关闭时释放像素缓冲，避免预览窗口开开关关把内存吃满
        setOnHidden(windowEvent -> {
            scaleTransition.stop();
            imageView.setImage(null);
        });
    }

    /**
     * 按预览需要的大小解码图片。
     * <p>
     * 预览最大也就铺满一块屏幕，没有必要把原图的全部像素都解码进内存
     * （一张 4000x3000 的照片约 45MB）。
     */
    private static Image loadPreviewImage(String url) {
        double screenHeight = ScreenUtil.getMaxScreenSize()[1];
        try {
            Image image = ImageUtil.loadForDisplay(url, screenHeight);
            if (image != null) {
                return image;
            }
        } catch (Throwable ignored) {
        }
        return new Image(url);
    }

    private void setImageSize(ImageView imageView, double width, double height) {
        Image image = imageView.getImage();
        if (image == null || image.getHeight() <= 0 || image.getWidth() <= 0
                || magnification > 1.05 || magnification < 0.95) {
            return;
        }
        // 调整横纵比
        width *= magnification;
        height *= magnification;
        double hr = height / image.getHeight();
        double wr = width / image.getWidth();
        if (hr > wr) {
            // 纵比更大则适应宽度
            imageView.setFitWidth(width);
            imageView.setFitHeight(image.getHeight() * wr);
        } else {
            // 横比更大则适应高度
            imageView.setFitWidth(image.getWidth() * hr);
            imageView.setFitHeight(height);
        }
    }

}
