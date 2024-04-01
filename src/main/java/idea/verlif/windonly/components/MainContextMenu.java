package idea.verlif.windonly.components;

import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.MessageUtil;
import idea.verlif.windonly.utils.StringTypeUtil;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;

import java.io.File;
import java.util.List;

public abstract class MainContextMenu<T extends Event> extends ContextMenu implements EventHandler<T> {

    private MenuItem edit;
    private MenuItem openWithSystem;
    private MenuItem openWithExplore;
    private MenuItem openWithBrowse;

    public MainContextMenu() {
        init();
    }

    public void init() {
        getItems().clear();
        edit = new MenuItem(MessageUtil.get("edit"));
        edit.setOnAction(event -> new Message(Message.What.EDIT).send(event));
        MenuItem copy = new MenuItem(MessageUtil.get("copy"));
        copy.setOnAction(event -> new Message(Message.What.COPY).send(event));
        MenuItem setToTop = new MenuItem(MessageUtil.get("setToTop"));
        setToTop.setOnAction(event -> new Message(Message.What.SET_TO_TOP).send(event));
        openWithSystem = new MenuItem(MessageUtil.get("openWithSystem"));
        openWithSystem.setOnAction(event -> new Message(Message.What.OPEN_WITH_SYSTEM).send(event));
        openWithExplore = new MenuItem(MessageUtil.get("openWithExplore"));
        openWithExplore.setOnAction(event -> new Message(Message.What.OPEN_WITH_EXPLORE).send(event));
        openWithBrowse = new MenuItem(MessageUtil.get("openWithBrowse"));
        openWithBrowse.setOnAction(event -> new Message(Message.What.OPEN_WITH_BROWSE).send(event));

        getItems().addAll(edit, copy, setToTop, openWithSystem, openWithExplore, openWithBrowse);
    }

    public abstract ProjectItem onItem();

    @Override
    public void handle(T t) {
        ProjectItem item = onItem();
        edit.setVisible(false);
        openWithSystem.setVisible(false);
        openWithExplore.setVisible(false);
        if (item != null) {
            edit.setVisible(item.getType() == ProjectItem.Type.TEXT);
            if (item.getType() == ProjectItem.Type.FILES) {
                List<File> list = (List<File>) item.getSource();
                if (list.size() == 1) {
                    openWithSystem.setVisible(true);
                }
            } else {
                openWithSystem.setVisible(item.getType() == ProjectItem.Type.FILE);
            }
            String url = null;
            if (item.getType() == ProjectItem.Type.IMAGE) {
                url = ((Image) item.getSource()).getUrl();
            } else if (item.getType() == ProjectItem.Type.TEXT) {
                url = item.getSource().toString();
            }
            openWithBrowse.setVisible(url != null && StringTypeUtil.isHtml(url));
        }
        openWithExplore.setVisible(openWithSystem.isVisible());
    }
}
