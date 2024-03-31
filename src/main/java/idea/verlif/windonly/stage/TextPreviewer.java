package idea.verlif.windonly.stage;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.utils.MessageUtil;
import idea.verlif.windonly.utils.ScreenUtil;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class TextPreviewer extends BaseStage {

    private final TextArea textArea;

    public TextPreviewer(String text) {
        super();

        this.textArea = createTextArea(text);
        getBorderPane().setCenter(textArea);
        initSize();
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

    private TextArea createTextArea(String text) {
        TextArea textArea = new TextArea(text);
        textArea.setFont(new Font(WindonlyConfig.getInstance().getFontSize()));
        textArea.setEditable(false);
        // 复制
        MenuItem copy = new MenuItem(MessageUtil.get("copy"));
        copy.setOnAction(actionEvent -> textArea.copy());
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
        // 添加菜单
        textArea.setContextMenu(new ContextMenu(copy, selectAll, lineWrap, pinTop));
        textArea.setOnContextMenuRequested(contextMenuEvent -> {
            copy.setDisable(textArea.getSelectedText().isEmpty());
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

}
