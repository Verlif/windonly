package idea.verlif.windonly.utils;

/**
 * 文本类型判断。
 * <p>
 * 原实现使用 {@code Pattern.matcher(text).matches()} 在任意长度的文本上判断是否为网址，
 * 一旦输入很长的文本（例如整段日志、base64），回溯开销会非常明显，
 * 而这里只是想知道"这段文本本身是不是一个链接"。
 * 因此改为一次线性扫描：不含空白字符，且以 {@code 协议://} 或 {@code www.} 开头。
 * <p>
 * 与原实现保持一致的两点：协议名只允许 ASCII 的 {@code [A-Za-z0-9_-]}
 * （避免"中文http://xxx"被判成链接），冒号后允许一到两个斜杠。
 *
 * @author Verlif
 */
public class StringTypeUtil {

    /**
     * 超过该长度的文本不可能是一个可点击的链接，直接判定为非链接
     */
    private static final int MAX_URL_LENGTH = 2048;

    public static boolean isHtml(String text) {
        if (text == null) {
            return false;
        }
        int length = text.length();
        if (length < 5 || length > MAX_URL_LENGTH) {
            return false;
        }
        // 链接中不允许出现空白字符，遇到第一个就退出
        for (int i = 0; i < length; i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return false;
            }
        }
        if (text.regionMatches(true, 0, "www.", 0, 4)) {
            return length > 4;
        }
        int schemeEnd = text.indexOf(':');
        if (schemeEnd < 1) {
            return false;
        }
        for (int i = 0; i < schemeEnd; i++) {
            char c = text.charAt(i);
            boolean asciiWord = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || c == '_' || c == '-';
            if (!asciiWord) {
                return false;
            }
        }
        // 冒号后必须至少有一个斜杠，且后面还有内容
        int after = schemeEnd + 1;
        if (after >= length || text.charAt(after) != '/') {
            return false;
        }
        after++;
        if (after < length && text.charAt(after) == '/') {
            after++;
        }
        return after < length;
    }
}
