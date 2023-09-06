package idea.verlif.windonly.components.item;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.ClipboardUtil;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Font;

public class TextItem extends Label implements Item<String> {

    private final String text;

    public TextItem(String s) {
        super(s);
        text = s;
    }

    @Override
    public void init() {
        setFont(Font.font(WindonlyConfig.getInstance().getFontSize()));
        setMaxHeight(200);
        setMaxWidth(3000);
        // 自动换行
        setStyle("-fx-wrap-text: true");

        // 添加拖拽处理器
        setOnDragDetected(event -> {
            Dragboard db = startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            db.setContent(content);
        });
    }

    @Override
    public String getSource() {
        return text;
    }

    @Override
    public boolean match(String key) {
        return text.contains(key);
    }

    @Override
    public boolean sourceEquals(String s) {
        return text.equals(s);
    }
}
