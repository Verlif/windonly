package idea.verlif.windonly.utils;

import idea.verlif.easy.dict.EasyDict;
import idea.verlif.easy.dict.properties.PropertiesDictProvider;

import java.io.File;
import java.net.URL;
import java.util.Locale;

public class MessageUtil {

    private static final EasyDict EASY_DICT;

    static {
        EASY_DICT = new EasyDict();
        PropertiesDictProvider provider = new PropertiesDictProvider();
        // 1. 开发环境（相对工程根目录）
        load(provider, "src\\main\\resources\\lang");
        // 2. 运行目录，解压即用的发行包里语言文件与 exe 同级
        load(provider, "lang");
        // 3. 程序自身所在目录。通过快捷方式启动时工作目录可能不是程序目录，
        //    这里再按可执行文件的位置找一次，保证语言文件一定能被读到。
        for (File dir : appDirectories()) {
            load(provider, new File(dir, "lang").getAbsolutePath());
        }
        // 4. 兜底：当前目录下的所有 properties
        load(provider, ".\\");
        EASY_DICT.addProvider(provider);
    }

    private MessageUtil() {
    }

    /**
     * 加载语言目录。easy-dict 在目录不存在时会抛异常，这里先做存在性判断。
     */
    private static void load(PropertiesDictProvider provider, String path) {
        File dir = new File(path);
        if (dir.isDirectory()) {
            provider.load(dir);
        }
    }

    /**
     * 程序所在目录。jpackage 生成的发行包中，应用 jar 位于 app 子目录下，
     * 语言文件位于其上一级，因此两个目录都会返回。
     */
    private static File[] appDirectories() {
        try {
            URL location = MessageUtil.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return new File[0];
            }
            File code = new File(location.toURI());
            File dir = code.isDirectory() ? code : code.getParentFile();
            if (dir == null) {
                return new File[0];
            }
            File parent = dir.getParentFile();
            return parent == null ? new File[]{dir} : new File[]{dir, parent};
        } catch (Throwable ignored) {
            return new File[0];
        }
    }

    public static String get(String code) {
        return EASY_DICT.get("zh").query(code);
    }

    public static String get(String code, Locale locale) {
        return EASY_DICT.get(locale.toLanguageTag()).query(code);
    }

    public static String get(String code, String tag) {
        return EASY_DICT.get(tag).query(code);
    }
}
