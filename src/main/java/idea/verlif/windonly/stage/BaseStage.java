package idea.verlif.windonly.stage;

import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.utils.MessageUtil;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class BaseStage extends Stage {

    private final BorderPane borderPane;
    private String tempText;

    public BaseStage() {
        BorderPane rootPane = new BorderPane();
        Scene scene = new Scene(rootPane);
        setScene(scene);
        setTitle(MessageUtil.get("app"));
        try (InputStream iconStream = getClass().getResourceAsStream("/images/icon.png")) {
            if (iconStream != null) {
                getIcons().add(new Image(iconStream));
            }
        } catch (IOException e) {
            throw new WindonlyException(e);
        }
        borderPane = new BorderPane();
        rootPane.setCenter(borderPane);

        // 置顶
        rootPane.setOnMousePressed(mouseEvent -> {
            // 窗口置顶快捷键
            if (mouseEvent.getButton() == MouseButton.MIDDLE) {
                if (isAlwaysOnTop()) {
                    unpinTop();
                } else {
                    pinTop();
                }
            }
        });
    }

    public void pinTop() {
        setAlwaysOnTop(true);
        showTip(MessageUtil.get("windowPin"));
    }

    public void unpinTop() {
        setAlwaysOnTop(false);
        closeTip();
    }

    protected BorderPane getBorderPane() {
        return borderPane;
    }

    private void showTip(String text) {
        tempText = getTitle();
        setTitle(tempText + " - " + text);
    }

    private void closeTip() {
        if (tempText != null) {
            setTitle(tempText);
        }
    }

}
