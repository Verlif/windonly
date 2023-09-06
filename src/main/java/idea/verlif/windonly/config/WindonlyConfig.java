package idea.verlif.windonly.config;

import idea.verlif.windonly.WindonlyApplication;

public class WindonlyConfig {

    private static final WindonlyConfig WINDONLY_CONFIG = new WindonlyConfig();

    private WindonlyConfig() {}

    public static WindonlyConfig getInstance() {
        return WINDONLY_CONFIG;
    }

    private double fontSize = 16;
    // 放大倍率
    private double magnification = 1.0;

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
}
