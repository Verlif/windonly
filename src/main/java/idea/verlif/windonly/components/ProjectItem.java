package idea.verlif.windonly.components;

import idea.verlif.windonly.components.item.Item;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

public class ProjectItem extends HBox implements Item<Object> {

    private final Item<Object> item;
    private final Node node;

    public ProjectItem(Node node) {
        this.node = node;
        this.item = (Item<Object>) node;

        init();
    }

    @Override
    public void init() {
        // 设置底线
        setPadding(new Insets(4));

        ObservableList<Node> children = getChildren();
        children.add(new OperateArea());
        children.add(node);
    }

    @Override
    public Object getSource() {
        return node;
    }

    public Object getTarget() {
        return item.getSource();
    }

    @Override
    public boolean match(String key) {
        return item.match(key);
    }

    @Override
    public boolean sourceEquals(Object o) {
        if (o.getClass() == item.getSource().getClass()) {
            return item.sourceEquals(o);
        } else {
            return false;
        }
    }

}
