package idea.verlif.windonly.components;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.MessageUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class OperateArea extends VBox {

    private static final String BUTTON_STYLE = "-fx-background-color: #bbebff;-fx-background-radius: 4";

    public OperateArea() {
        init();
    }

    public void init() {
        // 设置右边分割线
        setPadding(new Insets(2, 8 * WindonlyConfig.getInstance().getMagnification(), 2, 0));
        // 顶部居中布局
        setAlignment(Pos.TOP_CENTER);
        setSpacing(4);
        // 复制
        Label copyLabel = new Label(MessageUtil.get("copy"));
        copyLabel.setOnMouseClicked(mouseEvent -> new Message(Message.What.COPY).send(mouseEvent));
        copyLabel.setStyle(BUTTON_STYLE);
        copyLabel.setPadding(new ButtonInsets());
        // 置顶
        Label setToTop = new Label(MessageUtil.get("setToTop"));
        setToTop.setOnMouseClicked(mouseEvent -> new Message(Message.What.SET_TO_TOP).send(mouseEvent));
        setToTop.setStyle(BUTTON_STYLE);
        setToTop.setPadding(new ButtonInsets());
        // 删除
        Label delete = new Label(MessageUtil.get("delete"));
        delete.setOnMouseClicked(mouseEvent -> new Message(Message.What.DELETE).send(mouseEvent));
        delete.setStyle(BUTTON_STYLE);
        delete.setPadding(new ButtonInsets());

        getChildren().addAll(copyLabel, setToTop, delete);
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
