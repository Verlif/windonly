package idea.verlif.windonly.utils;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.data.Archive;

import java.io.File;

/**
 * @author Verlif
 */
public class FileTypeUtil {

    public static boolean isImage(File file) {
        String filename = file.getAbsolutePath().toLowerCase();
        for (String suffix : Archive.getSettings().getImages()) {
            if (filename.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isText(File file) {
        String filename = file.getName().toLowerCase();
        for (String suffix : Archive.getSettings().getTexts()) {
            if (filename.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

}
