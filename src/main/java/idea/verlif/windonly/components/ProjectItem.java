package idea.verlif.windonly.components;

import idea.verlif.windonly.components.item.Item;
import idea.verlif.windonly.manage.inner.Message;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ProjectItem extends HBox implements Item<Object> {

    private final Item<Object> item;
    private final Type type;

    public ProjectItem(Node node) {
        this.item = (Item<Object>) node;

        // 设定类型
        Object source = item.getSource();
        type = setType(source);

        init();
    }

    protected Node getOperateNode() {
        return new OperateArea();
    }

    @Override
    public void init() {
        // 设置底线
        setPadding(new Insets(4));

        ObservableList<Node> children = getChildren();
        children.add(getOperateNode());
        children.add((Node) item);
        setAlignment(Pos.CENTER_LEFT);
        // 添加拖拽处理器
        setOnDragDetected(event -> {
            Dragboard db = startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            if (type == Type.FILES) {
                content.putFiles((List<File>) item.getSource());
            } else if (type == Type.FILE) {
                content.putFiles(Collections.singletonList((File) item.getSource()));
            } else if (type == Type.IMAGE) {
                content.putImage((Image) item.getSource());
            } else {
                content.putString(item.getSource().toString());
            }
            db.setContent(content);
        });
        // 拖拽出框时默认失焦
        setOnDragExited(event -> new Message(Message.What.WINDOW_NOT_FOCUS).send());
    }

    @Override
    public void refresh() {
        ObservableList<Node> children = getChildren();
        for (Node child : children) {
            if (child instanceof Item<?> item) {
                item.refresh();
            }
        }
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

    public Item<Object> getItem() {
        return item;
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

    @Override
    public String toString() {
        return item.getSource().toString();
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
