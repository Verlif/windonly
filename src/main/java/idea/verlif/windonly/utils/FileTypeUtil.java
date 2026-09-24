package idea.verlif.windonly.utils;

import idea.verlif.windonly.data.Archive;

import java.io.File;
import java.util.Locale;

/**
 * @author Verlif
 */
public class FileTypeUtil {

    public static boolean isImage(File file) {
        if (file == null) {
            return false;
        }
        // 只用文件名做判断，避免每次都比较整条绝对路径（并且省掉一次大字符串创建）
        String name = file.getName().toLowerCase(Locale.ROOT);
        for (String suffix : Archive.getSettings().getImages()) {
            if (name.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isText(File file) {
        if (file == null) {
            return false;
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        for (String suffix : Archive.getSettings().getTexts()) {
            if (name.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

}
