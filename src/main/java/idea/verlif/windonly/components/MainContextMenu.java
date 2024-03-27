package idea.verlif.windonly.components;

import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.MessageUtil;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class MainContextMenu extends ContextMenu {

    public MainContextMenu() {
        init();
    }

    public void init() {
        getItems().clear();
        MenuItem copy = new MenuItem(MessageUtil.get("copy"));
        copy.setOnAction(event -> new Message(Message.What.COPY).send(event));
        MenuItem setToTop = new MenuItem(MessageUtil.get("setToTop"));
        setToTop.setOnAction(event -> new Message(Message.What.SET_TO_TOP).send(event));

        getItems().addAll(copy, setToTop);
    }
}
