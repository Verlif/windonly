package idea.verlif.windonly;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.ScreenUtil;
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
        // 自动存储
        stage.setOnHidden(windowEvent -> new Message(Message.What.ARCHIVE_SAVE).send());
        // 窗口聚焦事件
        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                new Message(Message.What.WINDOW_FOCUS).send();
            } else {
                new Message(Message.What.WINDOW_NOT_FOCUS).send();
            }
        });
        // 宽度初始化
        stage.setOnShown(windowEvent -> {
            new Message(Message.What.WINDOW_CHANGED_WIDTH).send(scene.getWidth());
        });
        // 鼠标拖拽进入时尝试弹出面板
        scene.setOnDragEntered(event -> {
            if (WindonlyConfig.getInstance().isSlide()) {
                new Message(Message.What.WINDOW_SLIDE_OUT).send();
            }
        });
        // 宽度变化
        scene.widthProperty().addListener((observableValue, oldVal, newVal) -> {
            new Message(Message.What.WINDOW_CHANGED_WIDTH).send(newVal);
        });
        // 注册消息处理
        register();
    }

    private static void register() {
        new Handler() {
            @Override
            public void handlerMessage(Message message) {
                switch (message.what) {
                    case Message.What.WINDOW_PIN:
                        Platform.runLater(() -> {
                            mainStage.setAlwaysOnTop(WindonlyConfig.getInstance().isAlwaysShow());
                        });
                        break;
                    case Message.What.WINDOW_SLIDE: {
                        if (WindonlyConfig.getInstance().isSlide()) {
                            double screenWidth = ScreenUtil.getScreenSize(mainStage)[0];
                            double thisWidth = mainStage.getWidth();
                            // 右侧则贴近右边框
                            left = !(mainStage.getX() + thisWidth / 2 > screenWidth / 2);
                            new Message(Message.What.WINDOW_SLIDE_OUT).send();
                        }
                    }
                    break;
                    case Message.What.WINDOW_SLIDE_OUT: {
                        if (left) {
                            slideLeft();
                        } else {
                            slideRight();
                        }
                    }
                    break;
                    case Message.What.WINDOW_SLIDE_IN: {
                        if (left) {
                            hideLeft();
                        } else {
                            hideRight();
                        }
                    }
                    break;
                    case Message.What.WINDOW_REQUIRE_HIDDEN: {
                        if (!mainStage.isFocused()) {
                            if (left) {
                                hideLeft();
                            } else {
                                hideRight();
                            }
                        }
                    }
                    break;
                    case Message.What.WINDOW_MIN: {
                        Platform.runLater(() -> mainStage.setMaximized(false));
                    }
                    break;
                    case Message.What.WINDOW_MAX: {
                        Platform.runLater(() -> mainStage.setMaximized(true));
                    }
                    break;
                    case Message.What.WINDOW_CLOSE: {
                        Platform.runLater(() -> mainStage.close());
                    }
                    break;
                }
            }
        };
    }

    private static void slideLeft() {
        mainStage.setX(-8 + ScreenUtil.getNowScreen(mainStage).getBounds().getMinX());
    }

    private static void hideLeft() {
        mainStage.setX(10 + ScreenUtil.getNowScreen(mainStage).getBounds().getMinX() - mainStage.getWidth());
    }

    private static void slideRight() {
        mainStage.setX(ScreenUtil.getNowScreen(mainStage).getBounds().getMaxX() - mainStage.getWidth() + 8);
    }

    private static void hideRight() {
        mainStage.setX(ScreenUtil.getNowScreen(mainStage).getBounds().getMaxX() - 10);
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
