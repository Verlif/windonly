package idea.verlif.windonly.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.data.Archive;
import idea.verlif.windonly.data.Savable;
import idea.verlif.windonly.manage.inner.Message;

public class WindonlyConfig implements Savable<String> {

    private static final WindonlyConfig WINDONLY_CONFIG = new WindonlyConfig();

    WindonlyConfig() {
    }

    public static WindonlyConfig getInstance() {
        return WINDONLY_CONFIG;
    }

    // 显示字体大小
    private double fontSize = 16;
    // 图片大小
    private double imageSize = fontSize * 8;
    // 放大倍率
    private double magnification = 1.0;
    // 是否置顶窗口
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
        return fontSize * magnification;
    }

    public void setFontSize(double fontSize) {
        initFontSize(fontSize);
        saveToFile();
    }

    private void initFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    public double getImageSize() {
        return imageSize;
    }

    public void setImageSize(double imageSize) {
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
        initMagnification(magnification);
        saveToFile();
    }

    private void initMagnification(double magnification) {
        this.magnification = magnification;
    }

    public boolean isAlwaysShow() {
        return alwaysShow;
    }

    public void setAlwaysShow(boolean alwaysShow) {
        initAlwaysShow(alwaysShow);
        saveToFile();
    }

    private void initAlwaysShow(boolean alwaysShow) {
        this.alwaysShow = alwaysShow;
        new Message(Message.What.WINDOW_PIN).send();
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        initLock(lock);
        saveToFile();
    }

    private void initLock(boolean lock) {
        this.lock = lock;
        new Message(Message.What.ARCHIVE_LOCK).send();
    }

    public boolean isSlide() {
        return slide;
    }

    public void setSlide(boolean slide) {
        initSlide(slide);
        saveToFile();
    }

    private void initSlide(boolean slide) {
        this.slide = slide;
        new Message(Message.What.WINDOW_SLIDE).send();
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
                JsonNode windonlyConfig = mapper.reader().readTree(s);
                if (windonlyConfig.has("alwaysShow")) {
                    initAlwaysShow(windonlyConfig.get("alwaysShow").asBoolean());
                }
                if (windonlyConfig.has("magnification")) {
                    initMagnification(windonlyConfig.get("magnification").asDouble());
                }
                if (windonlyConfig.has("fontSize")) {
                    initFontSize(windonlyConfig.get("fontSize").asDouble());
                }
                if (windonlyConfig.has("imageSize")) {
                    initImageSize(windonlyConfig.get("imageSize").asDouble());
                }
                if (windonlyConfig.has("lock")) {
                    initLock(windonlyConfig.get("lock").asBoolean());
                }
                if (windonlyConfig.has("slide")) {
                    initSlide(windonlyConfig.get("slide").asBoolean());
                }
            } catch (JsonProcessingException e) {
                throw new WindonlyException(e);
            }
        }
    }
}
