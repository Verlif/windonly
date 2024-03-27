package idea.verlif.windonly.components;

import javafx.scene.Node;

public class RemoteProjectItem extends ProjectItem {

    private final String key;

    public RemoteProjectItem(Node node, String key) {
        super(node);
        this.key = key;
    }

    @Override
    protected Node getOperateNode() {
        return new RemoteOperateArea();
    }

    public String getKey() {
        return key;
    }
}
