package idea.verlif.windonly.config;

import com.fasterxml.jackson.databind.JsonNode;
import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.data.Archive;
import idea.verlif.windonly.data.Savable;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.JsonUtil;

import java.math.BigDecimal;

public class WindonlyConfig implements Savable<String> {

    private static final WindonlyConfig WINDONLY_CONFIG = new WindonlyConfig();

    WindonlyConfig() {
    }

    public static WindonlyConfig getInstance() {
        return WINDONLY_CONFIG;
    }

    /**
     * 显示字体大小
     */
    private double fontSize = 16;
    /**
     * 按钮大小
     */
    private double buttonSize = 16;
    /**
     * 图片大小
     */
    private double imageSize = fontSize * 1;
    /**
     * 放大倍率
     */
    private double magnification = 1.0;
    /**
     * 允许显示的最大图片文件大小（单位byte）
     */
    private long displayImageMaxSize = 1024 * 1024;
    /**
     * 允许显示的最大文本字符数
     */
    private int displayTextMaxSize = 1024;
    /**
     * 允许显示的最大文件数量，超出数量则只显示省略号
     */
    private int displayFileNumber = 10;
    /**
     * 是否置顶窗口
     */
    private boolean alwaysShow = true;
    /**
     * 数据锁定，不允许数据更改，不允许删除工作区
     */
    private boolean lock = false;
    /**
     * 贴边收起
     */
    private boolean slide = false;

    public void saveToFile() {
        Archive archive = new Archive(Archive.getCurrentArchive());
        archive.save(WindonlyConfig.this);
    }

    public double getFontSize() {
        return fontSize * getMagnification();
    }

    public void setFontSize(double fontSize) {
        if (this.fontSize == fontSize) {
            return;
        }
        initFontSize(fontSize);
        saveToFile();
    }

    private void initFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    public double getButtonSize() {
        return buttonSize * getMagnification();
    }

    public void setButtonSize(double buttonSize) {
        if (this.buttonSize == buttonSize) {
            return;
        }
        initButtonSize(buttonSize);
        saveToFile();
    }

    public void initButtonSize(double buttonSize) {
        this.buttonSize = buttonSize;
    }

    public double getImageSize() {
        return imageSize * getMagnification();
    }

    public void setImageSize(double imageSize) {
        if (this.imageSize == imageSize) {
            return;
        }
        initImageSize(imageSize);
        saveToFile();
    }

    private void initImageSize(double imageSize) {
        this.imageSize = imageSize;
    }

    public double getMagnification() {
        return magnification;
    }

    public void setMagnification(double magnification) {
        if (this.magnification == magnification) {
            return;
        }
        initMagnification(magnification);
        saveToFile();
    }

    private void initMagnification(double magnification) {
        this.magnification = magnification;
    }

    public void changeMagnification(String magnification) {
        this.magnification = BigDecimal.valueOf(this.magnification).add(new BigDecimal(magnification)).doubleValue();
        if (this.magnification < 0.2) {
            this.magnification = 0.2;
        }
        saveToFile();
    }

    public long getDisplayImageMaxSize() {
        return displayImageMaxSize;
    }

    public void setDisplayImageMaxSize(long displayImageMaxSize) {
        if (this.displayImageMaxSize == displayImageMaxSize) {
            return;
        }
        initDisplayImageMaxSize(displayImageMaxSize);
        saveToFile();
    }

    public void initDisplayImageMaxSize(long displayImageMaxSize) {
        this.displayImageMaxSize = displayImageMaxSize;
    }

    public int getDisplayTextMaxSize() {
        return displayTextMaxSize;
    }

    public void setDisplayTextMaxSize(int displayTextMaxSize) {
        if (this.displayTextMaxSize == displayTextMaxSize) {
            return;
        }
        initDisplayTextMaxSize(displayTextMaxSize);
        saveToFile();
    }

    public void initDisplayTextMaxSize(int displayTextMaxSize) {
        this.displayTextMaxSize = displayTextMaxSize;
    }

    public int getDisplayFileNumber() {
        return displayFileNumber;
    }

    public void setDisplayFileNumber(int displayFileNumber) {
        if (this.displayFileNumber == displayFileNumber) {
            return;
        }
        initDisplayFileNumber(displayFileNumber);
        saveToFile();
    }

    public void initDisplayFileNumber(int displayFileNumber) {
        this.displayFileNumber = displayFileNumber;
    }

    public boolean isAlwaysShow() {
        return alwaysShow;
    }

    public void setAlwaysShow(boolean alwaysShow) {
        if (this.alwaysShow == alwaysShow) {
            return;
        }
        initAlwaysShow(alwaysShow);
        saveToFile();
    }

    private void initAlwaysShow(boolean alwaysShow) {
        boolean changed = this.alwaysShow != alwaysShow;
        this.alwaysShow = alwaysShow;
        if (changed) {
            new Message(Message.What.WINDOW_PIN).send();
        }
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        if (this.lock == lock) {
            return;
        }
        initLock(lock);
        saveToFile();
    }

    private void initLock(boolean lock) {
        boolean changed = this.lock != lock;
        this.lock = lock;
        if (changed) {
            new Message(Message.What.ARCHIVE_LOCK).send();
        }
    }

    public boolean isSlide() {
        return slide;
    }

    public void setSlide(boolean slide) {
        if (this.slide == slide) {
            return;
        }
        initSlide(slide);
        saveToFile();
    }

    private void initSlide(boolean slide) {
        boolean changed = this.slide != slide;
        this.slide = slide;
        if (changed) {
            new Message(Message.What.WINDOW_SLIDE).send();
        }
    }

    @Override
    public String save() {
        return JsonUtil.writePretty(this);
    }

    @Override
    public void load(String s) {
        if (s != null && !s.isEmpty()) {
            try {
                JsonNode windonlyConfig = JsonUtil.mapper().reader().readTree(s);
                if (windonlyConfig.has("alwaysShow")) {
                    initAlwaysShow(windonlyConfig.get("alwaysShow").asBoolean());
                }
                if (windonlyConfig.has("magnification")) {
                    // 注意：这里必须用 asDouble，放大倍率允许 0.2 这样的小数
                    initMagnification(windonlyConfig.get("magnification").asDouble());
                }
                if (windonlyConfig.has("displayImageMaxSize")) {
                    initDisplayImageMaxSize(windonlyConfig.get("displayImageMaxSize").asLong());
                }
                if (windonlyConfig.has("displayTextMaxSize")) {
                    initDisplayTextMaxSize(windonlyConfig.get("displayTextMaxSize").asInt());
                }
                if (windonlyConfig.has("displayFileNumber")) {
                    initDisplayFileNumber(windonlyConfig.get("displayFileNumber").asInt());
                }
                if (windonlyConfig.has("fontSize")) {
                    initFontSize(windonlyConfig.get("fontSize").asDouble());
                }
                if (windonlyConfig.has("imageSize")) {
                    initImageSize(windonlyConfig.get("imageSize").asDouble());
                }
                if (windonlyConfig.has("buttonSize")) {
                    initButtonSize(windonlyConfig.get("buttonSize").asDouble());
                }
                if (windonlyConfig.has("lock")) {
                    initLock(windonlyConfig.get("lock").asBoolean());
                }
                if (windonlyConfig.has("slide")) {
                    initSlide(windonlyConfig.get("slide").asBoolean());
                }
            } catch (Exception e) {
                throw new WindonlyException(e);
            }
        }
    }
}
