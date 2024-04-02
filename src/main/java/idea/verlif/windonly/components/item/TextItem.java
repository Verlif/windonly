package idea.verlif.windonly.components.item;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.stage.TextPreviewer;
import idea.verlif.windonly.utils.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.concurrent.atomic.AtomicReference;

public class TextItem extends Label implements Item<String> {

    private String text;

    public TextItem(String s) {
        this.text = s;
        int maxSize = WindonlyConfig.getInstance().getDisplayTextMaxSize();
        // 查看行数是否超标
        String[] split = s.split("\n", 5);
        if (split.length > 4) {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                text.append(split[i]).append("\n");
            }
            text.append("......");
            setText(text.toString());
            text.setLength(0);
        } else {
            if (text.length() > maxSize) {
                setText(text.substring(0, maxSize - 1) + "......");
            } else {
                setText(text);
            }
        }
    }

    @Override
    public void init() {
        setMaxWidth(ScreenUtil.getMaxScreenSize()[0] - 200);
        setMinWidth(100);
        // 自动换行
        setStyle("-fx-wrap-text: true");

        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() > 1) {
                if (mouseEvent.isControlDown() && StringTypeUtil.isHtml(text)) {
                    SystemExecUtil.openUrlByBrowser(text);
                } else {
                    new TextPreviewer(text).show();
                }
                mouseEvent.consume();
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
        Font font = Font.font(WindonlyConfig.getInstance().getFontSize());
        if (StringTypeUtil.isHtml(text)) {
            AtomicReference<Tooltip> tooltip = new AtomicReference<>();
            ScheduledUtil.execute(() -> tooltip.set(new Tooltip(UrlUtil.getTitleFromURL(text))),
                    () -> setTooltip(tooltip.get()));
            setTextFill(Color.rgb(67, 165, 255));
        }
        setFont(font);
    }

}
