package idea.verlif.windonly.stage;

import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.utils.MessageUtil;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class BaseStage extends Stage {

    /**
     * 窗口图标是固定资源，缓存一份即可。原实现每开一个预览窗口都会重新读一次 PNG 并解码。
     */
    private static Image appIcon;

    private final BorderPane borderPane;
    private String tempText;

    public BaseStage() {
        BorderPane rootPane = new BorderPane();
        Scene scene = new Scene(rootPane);
        setScene(scene);
        setTitle(MessageUtil.get("app"));
        Image icon = getAppIcon();
        if (icon != null) {
            getIcons().add(icon);
        }
        borderPane = new BorderPane();
        rootPane.setCenter(borderPane);

        // 初始化前台
        if (WindonlyConfig.getInstance().isAlwaysShow()) {
            pinTop();
        }
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

    private static synchronized Image getAppIcon() {
        if (appIcon == null) {
            try (InputStream iconStream = BaseStage.class.getResourceAsStream("/images/icon.png")) {
                if (iconStream != null) {
                    appIcon = new Image(iconStream);
                }
            } catch (IOException e) {
                throw new WindonlyException(e);
            }
        }
        return appIcon;
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
