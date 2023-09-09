package idea.verlif.windonly.components.alert;

import idea.verlif.windonly.WindonlyApplication;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.utils.MessageUtil;
import idea.verlif.windonly.utils.ScreenUtil;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;

public class TextAlert {

    private final TextInputDialog dialog;
    private final TextArea textArea;

    public TextAlert(String text) {
        this.dialog = new TextInputDialog();
        this.textArea = new TextArea(text);
        dialog.setResizable(true);
        dialog.setHeaderText(null);
        dialog.setGraphic(null);
        dialog.setTitle("");
        dialog.initOwner(WindonlyApplication.getMainStage());
        dialog.initModality(Modality.NONE);
        init();
    }

    private void init() {
        textArea.setFont(new Font(WindonlyConfig.getInstance().getFontSize()));
        textArea.setEditable(false);
        // 全选
        MenuItem selectAll = new MenuItem(MessageUtil.get("selectAll"));
        selectAll.setOnAction(actionEvent -> textArea.selectAll());
        // 自动换行设置
        MenuItem lineWrap = new MenuItem(MessageUtil.get("autoLineWrap"));
        lineWrap.setOnAction(actionEvent -> textArea.setWrapText(!textArea.isWrapText()));
        textArea.setContextMenu(new ContextMenu(selectAll, lineWrap));

        ObservableList<ButtonType> buttonTypes = dialog.getDialogPane().getButtonTypes();
        buttonTypes.remove(ButtonType.OK);
        buttonTypes.remove(ButtonType.CANCEL);
        buttonTypes.add(ButtonType.CLOSE);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(0, 5, 0, 5));
        double height = Math.min(
                textArea.getText().split("\n").length * (textArea.getFont().getSize() + 8),
                ScreenUtil.getScreenSize()[1] - 400
        );
        pane.setPrefHeight(height);
        pane.setCenter(textArea);
        dialog.getDialogPane().setContent(pane);
    }

    public void show() {
        dialog.show();
    }

    public void close() {
        dialog.close();
    }
}
