package idea.verlif.windonly.utils;

import idea.verlif.easy.language.MessageGetter;

import java.io.IOException;
import java.util.Locale;

public class MessageUtil {

    private static final MessageGetter MESSAGE_GETTER;

    static {
        MESSAGE_GETTER = new MessageGetter();
        try {
            MESSAGE_GETTER.addResource("src\\main\\resources\\lang");
            MESSAGE_GETTER.addResource("lang");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MessageUtil() {
    }

    public static String get(String code) {
        return MESSAGE_GETTER.get(code);
    }

    public static String get(String code, Locale locale) {
        return MESSAGE_GETTER.get(code, locale);
    }

    public static String get(String code, String tag) {
        return MESSAGE_GETTER.get(code, tag);
    }
}
