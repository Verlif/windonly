package idea.verlif.windonly.utils;

import java.io.File;

/**
 * @author Verlif
 */
public class FileTypeUtil {

    private static final String[] PICTURE_SUFFIX = {"png", "jpg", "jpeg", "bmp", "gif"};
    private static final String[] TEXT_SUFFIX = {"txt", "xml", "yml", "yaml", "properties", "json", "csv"};

    public static boolean isImage(File file) {
        String filename = file.getName().toLowerCase();
        for (String suffix : PICTURE_SUFFIX) {
            if (filename.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isText(File file) {
        String filename = file.getName().toLowerCase();
        for (String suffix : TEXT_SUFFIX) {
            if (filename.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

}
