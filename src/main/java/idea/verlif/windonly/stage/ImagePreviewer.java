package idea.verlif.windonly.stage;

import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.utils.MessageUtil;
import idea.verlif.windonly.utils.ScreenUtil;
import javafx.animation.ScaleTransition;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;

public class ImagePreviewer extends Stage {

    private final BorderPane borderPane;

    // 放大倍率
    private double magnification = 1.0;

    private double startX;
    private double imageStartX;
    private double startY;
    private double imageStartY;

    public ImagePreviewer(String url) {
        this(new Image(url));
    }

    public ImagePreviewer(Image image) {
        borderPane = new BorderPane();
        ImageView imageView = new ImageView(image);
        borderPane.setCenter(imageView);
        Scene scene = new Scene(borderPane);
        setScene(scene);
        setTitle("Windonly");
        try (InputStream iconStream = getClass().getResourceAsStream("/images/icon.png")) {
            if (iconStream != null) {
                getIcons().add(new Image(iconStream));
            }
        } catch (IOException e) {
            throw new WindonlyException(e);
        }
        // 设置初始化大小
        double[] screenSize = ScreenUtil.getScreenSize();
        if (screenSize[0] / 2 > image.getWidth() && screenSize[1] / 2 > image.getHeight()) {
            setImageSize(imageView, image.getWidth(), image.getHeight());
        } else {
            setImageSize(imageView, screenSize[0] / 2, screenSize[1] / 2);
        }

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
        imageView.setOnScroll(scrollEvent -> {
            double temp = magnification + scrollEvent.getDeltaY() / 20 * 0.1;
            magnification = Math.max(temp, 0.1);
            ScaleTransition st = new ScaleTransition(Duration.millis(500), imageView);
            st.setToX(magnification);
            st.setToY(magnification);
            st.play();
        });
        // 拖拽
        borderPane.setOnMousePressed(mouseEvent -> {
            // 窗口置顶快捷键
            if (mouseEvent.getButton() == MouseButton.MIDDLE) {
                setAlwaysOnTop(!isAlwaysOnTop());
                if (isAlwaysOnTop()) {
                    showTip(MessageUtil.get("windowPin"));
                } else {
                    closeTip();
                }
            } else {
                startX = mouseEvent.getX();
                imageStartX = imageView.getTranslateX();
                startY = mouseEvent.getY();
                imageStartY = imageView.getTranslateY();
            }
        });
        borderPane.setOnMouseDragged(mouseDragEvent -> {
            imageView.setTranslateX(imageStartX + mouseDragEvent.getX() - startX);
            imageView.setTranslateY(imageStartY + mouseDragEvent.getY() - startY);
        });
    }

    private void showTip(String text) {
        setTitle(text);
    }

    private void closeTip() {
        setTitle("Windonly");
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
