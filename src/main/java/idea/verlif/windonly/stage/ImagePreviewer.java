package idea.verlif.windonly.stage;

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

    public ImagePreviewer(String url) {
        this(new Image(url));
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
            ScaleTransition st = new ScaleTransition(Duration.millis(200), imageView);
            st.setToX(magnification);
            st.setToY(magnification);
            st.play();
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
    }

    private void setImageSize(ImageView imageView, double width, double height) {
        Image image = imageView.getImage();
        if (image == null || magnification > 1.05 || magnification < 0.95) {
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
