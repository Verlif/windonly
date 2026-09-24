package idea.verlif.windonly.data;

import idea.verlif.easy.file.util.FileUtil;
import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.utils.JsonUtil;
import idea.verlif.windonly.utils.MessageUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Archive implements Serializable {

    private static final String ARCHIVE_FILE_PATH = "archives";
    private static final String SETTING_CONFIG = "setting.config";
    private final String name;

    // 全局设置
    private static final Settings SETTINGS;

    static {
        File settingFile = new File(SETTING_CONFIG);
        Settings loaded = null;
        if (settingFile.exists()) {
            try {
                loaded = JsonUtil.read(FileUtil.readContentAsString(settingFile), Settings.class);
            } catch (Throwable e) {
                throw new WindonlyException("Cannot load Windonly config file - " + settingFile.getAbsolutePath());
            }
        }
        if (loaded != null) {
            SETTINGS = loaded;
        } else {
            // 写入文件
            SETTINGS = new Settings();
            saveSettings(SETTINGS);
        }
    }

    public Archive(String name) {
        this.name = name;
    }

    public void save(Savable<String> savable) {
        String archivePath = getArchivePath(name);
        File archiveDir = new File(archivePath);
        if (!archiveDir.exists() && !archiveDir.mkdirs()) {
            throw new WindonlyException("Cannot write Windonly config file - " + archiveDir.getAbsolutePath());
        }
        File file = new File(archivePath, getSavableFilename(savable));
        try {
            FileUtil.writeStringToFile(file, savable.save());
        } catch (Throwable e) {
            throw new WindonlyException("Cannot write Windonly config file - " + file.getAbsolutePath() + " - " + e.getMessage());
        }
    }

    public void load(Savable<String> savable) {
        String setting = getArchivePath(name);
        File file = new File(setting, getSavableFilename(savable));
        if (file.exists()) {
            try {
                savable.load(FileUtil.readContentAsString(file));
            } catch (Throwable e) {
                throw new WindonlyException("Cannot read Windonly config file - " + file.getAbsolutePath() + " - " + e.getMessage());
            }
        } else {
            savable.load("");
        }
    }

    private static void saveSettings(Settings settings) {
        File settingFile = new File(SETTING_CONFIG);
        try {
            FileUtil.writeStringToFile(settingFile, JsonUtil.writePretty(settings));
        } catch (Throwable e) {
            throw new WindonlyException("Cannot write Windonly config file - " + settingFile.getAbsolutePath() + " - " + e.getMessage());
        }
    }

    public static String getArchivePath(String name) {
        if (name != null) {
            return SETTINGS.archivePath + File.separator + name;
        } else {
            return SETTINGS.archivePath;
        }
    }

    private String getSavableFilename(Savable<?> savable) {
        return savable.getClass().getSimpleName() + ".config";
    }

    public static String getCurrentArchive() {
        if (SETTINGS.currentArchive == null) {
            SETTINGS.currentArchive = allArchives().get(0);
        }
        return SETTINGS.currentArchive;
    }

    public static void setCurrentArchive(String archive) {
        SETTINGS.setCurrentArchive(archive);
    }

    public static void delArchive(String archive) {
        FileUtil.deleteFile(new File(getArchivePath(archive)));
        if (archive.equals(SETTINGS.currentArchive)) {
            SETTINGS.currentArchive = allArchives().get(0);
        }
    }

    public static Settings getSettings() {
        return SETTINGS;
    }

    /**
     * 获取当前所有的存档
     */
    public static List<String> allArchives() {
        List<String> list = new ArrayList<>();
        String archivePath = getArchivePath(null);
        File file = new File(archivePath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        list.add(f.getName());
                    }
                }
            }
        }
        if (list.isEmpty()) {
            // 添加主工作区
            list.add(MessageUtil.get("mainArchive"));
        }
        return list;
    }

    public static boolean newArchive(String archive) {
        File file = new File(SETTINGS.archivePath, archive);
        return !file.exists() && file.mkdirs();
    }

    public static boolean renameArchive(String source, String target) {
        File file = new File(SETTINGS.archivePath, source);
        if (file.exists()) {
            return file.renameTo(new File(SETTINGS.archivePath, target));
        }
        return false;
    }

    public static final class Settings implements Serializable {

        private String archivePath = ARCHIVE_FILE_PATH;
        private String currentArchive = MessageUtil.get("mainArchive");
        private String[] images = {"png", "jpg", "jpeg", "bmp", "gif"};
        private String[] texts = {"txt", "xml", "yml", "yaml", "properties", "json", "csv"};

        public Settings() {
        }

        public String getArchivePath() {
            return archivePath;
        }

        public void setArchivePath(String archivePath) {
            if (Objects.equals(this.archivePath, archivePath)) {
                return;
            }
            this.archivePath = archivePath;
            Archive.saveSettings(this);
        }

        public String getCurrentArchive() {
            return currentArchive;
        }

        public void setCurrentArchive(String currentArchive) {
            // 值没变化时不落盘。切换/刷新工作区会反复调用这里，原实现每次都会写一次配置文件。
            if (Objects.equals(this.currentArchive, currentArchive)) {
                return;
            }
            this.currentArchive = currentArchive;
            Archive.saveSettings(this);
        }

        public String[] getImages() {
            return images;
        }

        public void setImages(String[] images) {
            this.images = images;
        }

        public String[] getTexts() {
            return texts;
        }

        public void setTexts(String[] texts) {
            this.texts = texts;
        }
    }
}
