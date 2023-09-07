package idea.verlif.windonly.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import idea.verlif.windonly.data.Savable;

public class WindonlyConfig implements Savable<String> {

    private static final WindonlyConfig WINDONLY_CONFIG = new WindonlyConfig();

    private WindonlyConfig() {
    }

    public static WindonlyConfig getInstance() {
        return WINDONLY_CONFIG;
    }

    // 显示字体大小
    private double fontSize = 16;
    // 放大倍率
    private double magnification = 1.0;
    // 是否置顶窗口
    private boolean alwaysShow = true;

    public double getFontSize() {
        return fontSize * magnification;
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    public double getImageSize() {
        return getFontSize() * 8;
    }

    public double getMagnification() {
        return magnification;
    }

    public void setMagnification(double magnification) {
        this.magnification = magnification;
    }

    public boolean isAlwaysShow() {
        return alwaysShow;
    }

    public void setAlwaysShow(boolean alwaysShow) {
        this.alwaysShow = alwaysShow;
    }

    @Override
    public String save() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(this);
        } catch (JsonProcessingException ignored) {
            return "";
        }
    }

    @Override
    public void load(String s) {
        if (s != null && !s.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                WindonlyConfig windonlyConfig = mapper.readValue(s, WindonlyConfig.class);
                setAlwaysShow(windonlyConfig.alwaysShow);
                setMagnification(windonlyConfig.magnification);
                setFontSize(windonlyConfig.fontSize);
            } catch (JsonProcessingException ignored) {
            }
        }
    }
}
