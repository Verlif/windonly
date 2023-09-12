package idea.verlif.windonly.components;

import idea.verlif.windonly.components.item.Item;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

import java.io.File;
import java.util.List;

public class RemoteProjectItem extends HBox implements Item<Object> {

    private final Item<Object> item;
    private final Node node;
    private final Type type;
    private final String key;

    public RemoteProjectItem(Node node, String key) {
        this.node = node;
        this.item = (Item<Object>) node;
        this.key = key;

        // 设定类型
        Object source = this.item.getSource();
        type = setType(source);

        init();
    }

    @Override
    public void init() {
        // 设置底线
        setPadding(new Insets(4));

        ObservableList<Node> children = getChildren();
        children.add(new RemoteOperateArea());
        children.add(node);
        // 添加拖拽处理器
        setOnDragDetected(event -> {
            Dragboard db = startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            if (type == Type.FILES) {
                content.putFiles((List<File>) item.getSource());
            } else if (type == Type.FILE) {
                content.putFiles(List.of((File) item.getSource()));
            } else if (type == Type.IMAGE) {
                content.putImage((Image) item.getSource());
            } else {
                content.putString(item.getSource().toString());
            }
            db.setContent(content);
        });
    }

    private Type setType(Object source) {
        if (source instanceof List) {
            return Type.FILES;
        } else if (source instanceof File) {
            return Type.FILE;
        } else if (source instanceof Image) {
            return Type.IMAGE;
        } else {
            return Type.TEXT;
        }
    }

    @Override
    public Object getSource() {
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

    public Type getType() {
        return type;
    }

    public enum Type {
        TEXT,
        FILE,
        FILES,
        IMAGE,
    }
}
