package idea.verlif.windonly.components;

import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.MessageUtil;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;

public abstract class MainContextMenu<T extends Event> extends ContextMenu implements EventHandler<T> {

    private final MenuItem edit;

    public MainContextMenu() {
        init();
        edit = new MenuItem(MessageUtil.get("edit"));
        edit.setOnAction(event -> new Message(Message.What.EDIT).send(event));
        getItems().add(edit);
    }

    public void init() {
        getItems().clear();
        MenuItem copy = new MenuItem(MessageUtil.get("copy"));
        copy.setOnAction(event -> new Message(Message.What.COPY).send(event));
        MenuItem setToTop = new MenuItem(MessageUtil.get("setToTop"));
        setToTop.setOnAction(event -> new Message(Message.What.SET_TO_TOP).send(event));

        getItems().addAll(copy, setToTop);
    }

    public MenuItem getEdit() {
        return edit;
    }
}
