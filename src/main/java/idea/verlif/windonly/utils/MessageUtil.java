package idea.verlif.windonly.utils;


import idea.verlif.easy.dict.EasyDict;
import idea.verlif.easy.dict.properties.PropertiesDictProvider;

import java.util.Locale;

public class MessageUtil {

    private static final EasyDict EASY_DICT;

    static {
        EASY_DICT = new EasyDict();
        PropertiesDictProvider provider = new PropertiesDictProvider();
        provider.load("src\\main\\resources\\lang");
        provider.load("lang");
        provider.load(".\\");
        EASY_DICT.addProvider(provider);
    }

    private MessageUtil() {
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
