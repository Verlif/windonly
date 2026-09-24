package idea.verlif.windonly;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.ScreenUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class WindonlyApplication extends Application {

    private static Stage mainStage;
    private static boolean left;
    /**
     * 窗口当前是否已经收起在屏幕外。
     * 原实现每次移动窗口都会重新 setX，拖动、悬停等高频事件下会产生大量无意义的窗口操作，
     * 这里记录状态，位置没有变化时直接跳过。
     */
    private static boolean hidden;

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
        // 注册消息处理（必须在 show 之前，窗口显示的初始化消息需要被处理）
        register();
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
            // 开启贴边时把窗口贴到对应屏幕边缘
            if (WindonlyConfig.getInstance().isSlide()) {
                new Message(Message.What.WINDOW_SLIDE).send();
            }
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
    }

    private static void register() {
        new Handler() {
            @Override
            public void handlerMessage(Message message) {
                switch (message.what) {
                    case Message.What.WINDOW_PIN:
                        mainStage.setAlwaysOnTop(WindonlyConfig.getInstance().isAlwaysShow());
                        break;
                    case Message.What.WINDOW_SLIDE: {
                        double screenWidth = ScreenUtil.getScreenSize(mainStage)[0];
                        double thisWidth = mainStage.getWidth();
                        // 右侧则贴近右边框
                        left = !(mainStage.getX() + thisWidth / 2 > screenWidth / 2);
                        // 关闭贴边时同样要把窗口拉回屏幕内，
                        // 否则窗口会一直停留在收起位置（原实现只在开启贴边时处理）。
                        slideOut();
                    }
                    break;
                    case Message.What.WINDOW_SLIDE_OUT:
                        slideOut();
                        break;
                    case Message.What.WINDOW_SLIDE_IN:
                        if (left) {
                            hideLeft();
                        } else {
                            hideRight();
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
                    case Message.What.WINDOW_MIN:
                        mainStage.setMaximized(false);
                        break;
                    case Message.What.WINDOW_MAX:
                        mainStage.setMaximized(true);
                        break;
                    case Message.What.WINDOW_CLOSE:
                        mainStage.close();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private static void slideOut() {
        if (left) {
            slideLeft();
        } else {
            slideRight();
        }
    }

    private static void slideLeft() {
        moveTo(-8 + ScreenUtil.getNowScreen(mainStage).getBounds().getMinX(), false);
    }

    private static void hideLeft() {
        moveTo(10 + ScreenUtil.getNowScreen(mainStage).getBounds().getMinX() - mainStage.getWidth(), true);
    }

    private static void slideRight() {
        moveTo(ScreenUtil.getNowScreen(mainStage).getBounds().getMaxX() - mainStage.getWidth() + 8, false);
    }

    private static void hideRight() {
        moveTo(ScreenUtil.getNowScreen(mainStage).getBounds().getMaxX() - 10, true);
    }

    /**
     * 移动到目标位置。位置和收起状态都没有变化时不重复设置，避免多余的窗口重排。
     */
    private static void moveTo(double x, boolean hide) {
        if (hidden == hide && Math.abs(mainStage.getX() - x) < 0.5) {
            return;
        }
        hidden = hide;
        mainStage.setX(x);
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
