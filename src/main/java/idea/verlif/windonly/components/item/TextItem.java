package idea.verlif.windonly.components.item;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.stage.TextPreviewer;
import idea.verlif.windonly.utils.ScreenUtil;
import idea.verlif.windonly.utils.StringTypeUtil;
import idea.verlif.windonly.utils.SystemExecUtil;
import idea.verlif.windonly.utils.UrlUtil;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TextItem extends Label implements Item<String> {

    /**
     * 最多显示的文本行数
     */
    private static final int MAX_LINE = 4;

    private static final Color LINK_COLOR = Color.rgb(67, 165, 255);
    private static final Color TEXT_COLOR = Color.BLACK;

    private String text;
    /**
     * 复用同一个 Tooltip 实例：原实现每次 refresh 都会新建 Tooltip，
     * 过滤列表时会产生大量短命对象。
     */
    private Tooltip tooltip;
    /**
     * 链接标题是否已经获取过
     */
    private boolean titleLoaded;

    public TextItem(String s) {
        this.text = s;
    }

    @Override
    public void init() {
        setMaxWidth(ScreenUtil.getMaxScreenSize()[0] - 200);
        setMinWidth(100);

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
        // 网页标题只在鼠标移入时才请求：原实现是列表加载时对每个链接都发一次 HTTP 请求，
        // 数据一多就会同时占用网络与内存，而且用户根本还没看到。
        setOnMouseEntered(mouseEvent -> loadLinkTitle());
        refresh();
    }

    @Override
    public String getSource() {
        return text;
    }

    @Override
    public void setSource(String s) {
        this.text = s;
        this.titleLoaded = false;
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
        int maxSize = WindonlyConfig.getInstance().getDisplayTextMaxSize();
        // 原实现用 split("\n", 5) 来数行，大文本会直接复制出一个数组
        int newlineCount = 0;
        int cut = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n' && ++newlineCount == MAX_LINE) {
                cut = i;
                break;
            }
        }
        boolean overSize = cut >= 0;
        String display;
        if (overSize) {
            display = text.substring(0, cut + 1) + "......";
        } else if (text.length() > maxSize) {
            display = text.substring(0, maxSize - 1) + "......";
        } else {
            display = text;
        }
        setText(display);

        Tooltip tip = getOrCreateTooltip();
        if (StringTypeUtil.isHtml(text)) {
            String title = UrlUtil.cachedTitle(text);
            titleLoaded = title != null;
            tip.setText(title != null ? title : text);
            setTextFill(LINK_COLOR);
            setUnderline(true);
        } else {
            if (overSize) {
                tip.setText(display + " +" + (text.length() - display.length()));
            } else {
                tip.setText(text);
            }
            setTextFill(TEXT_COLOR);
            setUnderline(false);
        }
        setFont(Font.font(WindonlyConfig.getInstance().getFontSize()));
    }

    private Tooltip getOrCreateTooltip() {
        Tooltip tip = tooltip;
        if (tip == null) {
            tip = new Tooltip();
            tooltip = tip;
            setTooltip(tip);
        }
        return tip;
    }

    private void loadLinkTitle() {
        if (titleLoaded || !StringTypeUtil.isHtml(text)) {
            return;
        }
        titleLoaded = true;
        String target = text;
        UrlUtil.requestTitle(target, title -> {
            Tooltip tip = tooltip;
            // 数据可能已经被替换，确认还是同一条文本再更新
            if (tip != null && target.equals(this.text)) {
                tip.setText(title);
            }
        });
    }
}
