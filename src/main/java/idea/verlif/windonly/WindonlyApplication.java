package idea.verlif.windonly;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.ScreenUtil;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;

public class WindonlyApplication extends Application {

    private static Stage mainStage;
    private static boolean left;

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
        stage.setOnHidden(windowEvent -> new Message(Message.What.ARCHIVE_SAVE).send());
        // 注册消息处理
        register();
    }

    private static void register() {
        new Handler() {
            @Override
            public void handlerMessage(Message message) {
                switch (message.what) {
                    case Message.What.WINDOW_PIN -> Platform.runLater(() -> {
                        mainStage.setAlwaysOnTop(WindonlyConfig.getInstance().isAlwaysShow());
                    });
                    case Message.What.WINDOW_SLIDE -> {
                        if (WindonlyConfig.getInstance().isSlide()) {
                            double screenWidth = ScreenUtil.getScreenSize()[0];
                            double thisWidth = mainStage.getWidth();
                            // 右侧则贴近右边框
                            left = !(mainStage.getX() + thisWidth / 2 > screenWidth / 2);
                            new Message(Message.What.WINDOW_SLIDE_OUT).send();
                        }
                    }
                    case Message.What.WINDOW_SLIDE_OUT -> {
                        if (left) {
                            slideLeft();
                        } else {
                            slideRight();
                        }
                    }
                    case Message.What.WINDOW_SLIDE_IN -> {
                        if (left) {
                            hideLeft();
                        } else {
                            hideRight();
                        }
                    }
                    case Message.What.WINDOW_MIN -> {
                        Platform.runLater(() -> mainStage.setMaximized(false));
                    }
                    case Message.What.WINDOW_MAX -> {
                        Platform.runLater(() -> mainStage.setMaximized(true));
                    }
                    case Message.What.WINDOW_CLOSE -> {
                        Platform.runLater(() -> mainStage.close());
                    }
                }
            }
        };
    }

    private static void slideLeft() {
        mainStage.setX(-8);
    }

    private static void hideLeft() {
        mainStage.setX(10 - mainStage.getWidth());
    }

    private static void slideRight() {
        mainStage.setX(ScreenUtil.getScreenSize()[0] - mainStage.getWidth() + 8);
    }

    private static void hideRight() {
        mainStage.setX(ScreenUtil.getScreenSize()[0] - 10);
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
