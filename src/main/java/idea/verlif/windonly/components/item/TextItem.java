package idea.verlif.windonly.components.item;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.stage.TextPreviewer;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class TextItem extends Label implements Item<String> {

    private String text;

    public TextItem(String s) {
        super(s);
        text = s;
    }

    @Override
    public void init() {
        setMaxHeight(200);
        setMaxWidth(3000);
        // 自动换行
        setStyle("-fx-wrap-text: true");

        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() > 1) {
                new TextPreviewer(text).show();
            }
        });
        refresh();
    }

    @Override
    public String getSource() {
        return text;
    }

    @Override
    public void setSource(String s) {
        this.text = s;
    }

    @Override
    public boolean match(String key) {
        return text.contains(key);
    }

    @Override
    public boolean sourceEquals(String s) {
        return text.equals(s);
    }

    @Override
    public void refresh() {
        setFont(Font.font(WindonlyConfig.getInstance().getFontSize()));
    }
}
