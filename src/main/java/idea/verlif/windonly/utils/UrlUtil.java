package idea.verlif.windonly.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {

    private static final Pattern PATTERN_TITLE = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE);

    /**
     * 最多读取的响应字节数。标题一定在文档开头，没必要把整页 HTML 读进内存。
     */
    private static final int MAX_READ_CHARS = 64 * 1024;

    /**
     * 标题缓存上限，防止长时间运行后无限增长。
     */
    private static final int CACHE_LIMIT = 256;

    private static final Map<String, String> TITLE_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > CACHE_LIMIT;
                }
            });

    /**
     * 正在请求中的地址，避免同一地址被并发重复请求
     */
    private static final Set<String> LOADING = ConcurrentHashMap.newKeySet();

    /**
     * 获取已缓存的标题，没有则返回 null。
     */
    public static String cachedTitle(String url) {
        return url == null ? null : TITLE_CACHE.get(url);
    }

    /**
     * 异步获取网页标题。同一个地址只会真正请求一次，结果会被缓存。
     * 无论成功与否都会通过 {@code callback} 回调（在 FX 线程）。
     *
     * @param url      网页地址
     * @param callback 结果回调，失败时返回原地址
     */
    public static void requestTitle(String url, Consumer<String> callback) {
        if (url == null || callback == null) {
            return;
        }
        String cached = TITLE_CACHE.get(url);
        if (cached != null) {
            callback.accept(cached);
            return;
        }
        if (!LOADING.add(url)) {
            // 已经有相同请求在路上，等待它写缓存后由调用方自行刷新即可
            return;
        }
        ScheduledUtil.execute(() -> {
            String title = fetchTitle(url);
            TITLE_CACHE.put(url, title);
            LOADING.remove(url);
        }, () -> callback.accept(TITLE_CACHE.getOrDefault(url, url)));
    }

    private static String fetchTitle(String urlString) {
        InputStream stream = null;
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            stream = conn.getInputStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder(256);
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    Matcher matcher = PATTERN_TITLE.matcher(builder);
                    if (matcher.find()) {
                        return matcher.group(1).trim();
                    }
                    if (builder.length() > MAX_READ_CHARS) {
                        break;
                    }
                }
            }
        } catch (IOException | RuntimeException ignored) {
            // 网络不可用、地址非法等都属于正常情况
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return urlString;
    }
}
