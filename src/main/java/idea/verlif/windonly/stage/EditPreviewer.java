package idea.verlif.windonly.stage;

import idea.verlif.windonly.components.ProjectItem;
import idea.verlif.windonly.components.item.Item;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.MessageUtil;
import idea.verlif.windonly.utils.ScreenUtil;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;

public class EditPreviewer extends BaseStage {

    private final TextArea textArea;
    private final ProjectItem item;
    /**
     * 是否已更改
     */
    private boolean changed = false;

    public EditPreviewer(ProjectItem item) {
        super();
        this.item = item;
        setTitle(MessageUtil.get("edit"));
        if (item.getType() == ProjectItem.Type.TEXT) {
            initModality(Modality.APPLICATION_MODAL);
            // 初始化文本
            String text = (String) item.getSource();
            // 创建文本域
            this.textArea = createEditArea(text);
            // 设置文本域
            getBorderPane().setCenter(textArea);
            // 初始化尺寸
            initSize();
            // 关闭时有更新就触发列表更新
            setOnHidden(windowEvent -> {
                if (changed) {
                    new Message(Message.What.DATA_REFRESH).send();
                }
            });
        } else {
            this.textArea = null;
            close();
        }
    }

    private void initSize() {
        BorderPane borderPane = getBorderPane();
        borderPane.setPadding(new Insets(0, 5, 5, 5));
        double max = ScreenUtil.getScreenSize(this)[1] - 400;
        double preHeight = textArea.getText().split("\n").length * (textArea.getFont().getSize() + 8);
        double height = Math.min(
                preHeight + 200,
                max
        );
        borderPane.setPrefHeight(height);
    }

    private TextArea createEditArea(String text) {
        TextArea textArea = new TextArea(text);
        textArea.setFont(new Font(WindonlyConfig.getInstance().getCalcFontSize()));
        textArea.setEditable(true);
        textArea.textProperty().addListener((observableValue, s, t1) -> {
            EditPreviewer.this.setTitle(MessageUtil.get("edit") + "*");
        });
        textArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.S) {
                save();
            }
        });
        // 全选
        MenuItem selectAll = new MenuItem(MessageUtil.get("selectAll"));
        selectAll.setOnAction(actionEvent -> textArea.selectAll());
        // 自动换行设置
        MenuItem lineWrap = new MenuItem(MessageUtil.get("autoLineWrap"));
        lineWrap.setOnAction(actionEvent -> textArea.setWrapText(!textArea.isWrapText()));
        // 置顶
        MenuItem pinTop = new MenuItem(MessageUtil.get("setToTop"));
        pinTop.setOnAction(actionEvent -> {
            if (isAlwaysOnTop()) {
                unpinTop();
            } else {
                pinTop();
            }
        });
        // 保存
        MenuItem save = new MenuItem(MessageUtil.get("save"));
        save.setOnAction(actionEvent -> save());
        // 添加菜单
        textArea.setContextMenu(new ContextMenu(save, selectAll, lineWrap, pinTop));
        textArea.setOnContextMenuRequested(contextMenuEvent -> {
            lineWrap.setText((textArea.isWrapText() ? "ON -" : "OFF-") + MessageUtil.get("autoLineWrap"));
            pinTop.setText((isAlwaysOnTop() ? "ON -" : "OFF-") + MessageUtil.get("setToTop"));
        });
        textArea.setStyle(".text-area{" +
                "    -fx-background-insets: 0;" +
                "    -fx-background-color: transparent, white, transparent, white;" +
                "}" +
                ".text-area .content {" +
                "    -fx-background-color: transparent, white, transparent, white;" +
                "}" +
                ".text-area:focused {" +
                "    -fx-background-color: transparent, white, transparent, white;" +
                "}");
        return textArea;
    }

    private void save() {
        this.changed = true;
        Item<Object> objectItem = item.getItem();
        objectItem.setSource(textArea.getText());
        new Message(Message.What.ARCHIVE_SAVE).send();
        EditPreviewer.this.setTitle(MessageUtil.get("edit"));
    }
}
