package idea.verlif.windonly.components.item;

import idea.verlif.windonly.config.WindonlyConfig;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
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
