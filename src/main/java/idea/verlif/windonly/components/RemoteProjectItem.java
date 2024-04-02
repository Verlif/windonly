package idea.verlif.windonly.components;

import idea.verlif.windonly.components.item.Item;
import idea.verlif.windonly.manage.inner.Message;
import javafx.scene.Node;

public class RemoteProjectItem extends ProjectItem {

    private final String key;

    public RemoteProjectItem(Node node, String key) {
        super(node);
        this.key = key;
    }

    @Override
    public void init() {
        super.init();
        setOnMouseClicked(event -> {
            if (event.isMiddleButtonDown()) {
                new Message(Message.What.COPY_REMOTE).send();
            }
        });
    }

    @Override
    protected Node getOperateNode() {
        return new RemoteOperateArea();
    }

    public String getKey() {
        return key;
    }
}
