package idea.verlif.windonly.utils;

import java.util.regex.Pattern;

public class StringTypeUtil {

    private static final Pattern NET_URL = Pattern.compile("\\b(([\\w-]+://?|www[.])[^\\s()<>]+(?:\\([\\w\\d]+\\)|([^[:punct]\\s]|/)))");

    public static boolean isHtml(String text) {
        return NET_URL.matcher(text).matches();
    }
}
