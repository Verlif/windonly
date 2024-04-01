package idea.verlif.windonly.utils;

import idea.verlif.windonly.WindonlyException;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

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

    public static boolean selectFileByExplorer(String path) {
        try {
            exec("explorer.exe /select, " + path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean openUrlByBrowser(String url) {
        URI uri = URI.create(url);
        // 获取当前系统桌面扩展
        Desktop dp = Desktop.getDesktop();
        // 判断系统桌面是否支持要执行的功能
        if (dp.isSupported(Desktop.Action.BROWSE)) {
            // 获取系统默认浏览器打开链接
            try {
                dp.browse(uri);
                return true;
            } catch (IOException e) {
                throw new WindonlyException(e);
            }
        }
        return false;
    }
}
