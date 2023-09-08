package idea.verlif.windonly;

import idea.verlif.easy.language.handler.MessageHandler;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class WindonlyApplication extends Application {

    private static Stage mainStage;

    @Override
    public void start(Stage stage) throws IOException {
        mainStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(WindonlyApplication.class.getResource("fxml/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Windonly");
        try (InputStream iconStream = getClass().getResourceAsStream("/images/icon.png")) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        }
        stage.setScene(scene);
        stage.setAlwaysOnTop(WindonlyConfig.getInstance().isAlwaysShow());
        stage.show();

        // 注册消息处理
        register();
    }

    private static void register() {
        new Handler() {
            @Override
            public void handlerMessage(Message message) {
                if (message.what == Message.What.WINDOW_PIN) {
                    Platform.runLater(() -> {
                        mainStage.setAlwaysOnTop(WindonlyConfig.getInstance().isAlwaysShow());
                    });
                }
            }
        };
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
