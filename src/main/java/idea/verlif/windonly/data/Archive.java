package idea.verlif.windonly.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import idea.verlif.easy.file.util.FileUtil;
import idea.verlif.windonly.WindonlyException;
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
        ObjectMapper mapper = new ObjectMapper();
        if (settingFile.exists()) {
            try {
                Settings settings = mapper.readValue(FileUtil.readContentAsString(settingFile), Settings.class);
                SETTINGS = Objects.requireNonNullElseGet(settings, Settings::new);
            } catch (IOException e) {
                throw new WindonlyException("Cannot load Windonly config file - " + settingFile.getAbsolutePath());
            }
        } else {
            // 写入文件
            SETTINGS = new Settings();
            try {
                String value = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(SETTINGS);
                FileUtil.writeStringToFile(settingFile, value);
            } catch (IOException e) {
                throw new WindonlyException("Cannot write Windonly config file - " + settingFile.getAbsolutePath() + " - " + e.getMessage());
            }
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
        ObjectMapper mapper = new ObjectMapper();
        File settingFile = new File(SETTING_CONFIG);
        try {
            String value = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(settings);
            FileUtil.writeStringToFile(settingFile, value);
        } catch (IOException e) {
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
            this.archivePath = archivePath;
            Archive.saveSettings(this);
        }

        public String getCurrentArchive() {
            return currentArchive;
        }

        public void setCurrentArchive(String currentArchive) {
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
