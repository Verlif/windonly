package idea.verlif.windonly.utils;

import java.io.IOException;

public class SystemExecUtil {

    public static void exec(String line) throws IOException {
        Runtime.getRuntime().exec(line);
    }

    public static boolean openFileByExplorer(String path) {
        try {
            exec("explorer.exe " + path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
