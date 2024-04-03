package idea.verlif.windonly.utils;

import idea.verlif.windonly.WindonlyException;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Pattern;

public class SystemExecUtil {

    private static final Pattern NUMBER_START = Pattern.compile("[1-2].*");

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
        // 对直接的
        if (NUMBER_START.matcher(url).matches()) {
            url = "http://" + url;
        }
        try {
            URI uri = URI.create(url);
            // 获取当前系统桌面扩展
            Desktop dp = Desktop.getDesktop();
            // 判断系统桌面是否支持要执行的功能
            if (dp.isSupported(Desktop.Action.BROWSE)) {
                // 获取系统默认浏览器打开链接
                dp.browse(uri);
                return true;
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new WindonlyException(e);
        }
        return false;
    }
}
