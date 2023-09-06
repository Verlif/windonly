package idea.verlif.windonly.components;

import idea.verlif.windonly.utils.MessageUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.input.MouseEvent;

public abstract class ConfirmAlert extends Alert {

    public ConfirmAlert(AlertType alertType, String s) {
        this(alertType, s, null);
    }

    public ConfirmAlert(AlertType alertType, String s, MouseEvent mouseEvent) {
        super(alertType, s);

        if (mouseEvent != null) {
            // 定位鼠标位置
            setOnShowing(dialogEvent -> {
                setX(mouseEvent.getX());
                setY(mouseEvent.getY());
            });
        }
    }

    public abstract void sure();

    private final class SureMenu extends MenuButton {

        public final SureMenu SURE_MENU = new SureMenu();

        public SureMenu() {
            super(MessageUtil.get("sure"));

            setOnAction(actionEvent -> sure());
        }
    }

    private final class CancelMenu extends MenuButton {

        public final SureMenu CANCEL_MENU = new SureMenu();

        public CancelMenu() {
            super(MessageUtil.get("cancel"));

            setOnAction(actionEvent -> hide());
        }
    }
}
