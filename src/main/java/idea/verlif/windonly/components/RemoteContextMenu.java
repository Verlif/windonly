package idea.verlif.windonly.components;

import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.MessageUtil;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class RemoteContextMenu extends ContextMenu {

    public RemoteContextMenu() {
        init();
    }

    public void init() {
        getItems().clear();
        MenuItem copy = new MenuItem(MessageUtil.get("copy"));
        copy.setOnAction(event -> new Message(Message.What.COPY_REMOTE).send(event));

        getItems().addAll(copy);
    }
}
