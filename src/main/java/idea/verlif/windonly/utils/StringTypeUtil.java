package idea.verlif.windonly.utils;

import java.util.regex.Pattern;

public class StringTypeUtil {

    private static final Pattern NET_URL = Pattern.compile("^\\w+[^\\s]+(\\.[^\\s]+){1,}$");

    public static boolean isHtml(String text) {
        return NET_URL.matcher(text).matches();
    }
}
