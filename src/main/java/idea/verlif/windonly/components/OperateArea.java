package idea.verlif.windonly.components;

import idea.verlif.windonly.components.item.Item;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.MessageUtil;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class OperateArea extends VBox implements Item<Node> {

    private static final String BUTTON_STYLE = "-fx-background-color: #bbebff;-fx-background-radius: 4";

    public OperateArea() {
        init();
    }

    public void init() {
        // 顶部居中布局
        setAlignment(Pos.TOP_CENTER);
        setSpacing(4);
        refresh();
    }

    @Override
    public Node getSource() {
        return this;
    }

    @Override
    public boolean match(String key) {
        return false;
    }

    @Override
    public boolean sourceEquals(Node node) {
        return false;
    }

    @Override
    public void refresh() {
        // 设置右边分割线
        setPadding(new Insets(2, 8 * WindonlyConfig.getInstance().getMagnification(), 2, 0));
        ObservableList<Node> children = getChildren();
        children.clear();
        // 删除
        Label delete = new DeleteButton();
        children.addAll(delete);
    }

    private static final class DeleteButton extends Label {

        private boolean confirmDelete;

        public DeleteButton() {
            super(MessageUtil.get("delete"));
            setOnMouseClicked(mouseEvent -> {
                if (confirmDelete) {
                    new Message(Message.What.DELETE).send(mouseEvent);
                } else {
                    requestDelete();
                }
            });
            setOnMouseExited(mouseEvent -> reset());
            setStyle(BUTTON_STYLE);
            setPadding(new ButtonInsets());
        }

        private void requestDelete() {
            confirmDelete = true;
            setStyle("-fx-background-color: #ff5858;;-fx-background-radius: 4");
        }

        private void reset() {
            confirmDelete = false;
            setStyle(BUTTON_STYLE);
        }
    }

    private static final class ButtonInsets extends Insets {

        public ButtonInsets() {
            super(4 * WindonlyConfig.getInstance().getMagnification(),
                    8 * WindonlyConfig.getInstance().getMagnification(),
                    4 * WindonlyConfig.getInstance().getMagnification(),
                    8 * WindonlyConfig.getInstance().getMagnification());
        }
    }
}
