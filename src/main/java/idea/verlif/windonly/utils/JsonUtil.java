package idea.verlif.windonly.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 统一的 JSON 读写入口。
 * <p>
 * 原实现里每个存档类都会在使用时 {@code new ObjectMapper()}，而 ObjectMapper 的构建
 * 需要扫描并注册大量序列化器，属于典型的重对象。列表保存、配置保存、远程消息收发
 * 都会触发它，频繁创建会造成明显的临时对象与 GC 压力。
 * <p>
 * ObjectMapper 在配置完成后是线程安全的，因此这里全局共用一个实例：
 * 磁盘存档使用带缩进的格式（方便排查），网络消息使用紧凑格式（减少传输与内存占用）。
 *
 * @author Verlif
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            // 存档新增字段时不应该导致旧版本直接读取失败
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonUtil() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * 写出紧凑 JSON，适合网络传输等场景。
     */
    public static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /**
     * 写出带缩进的 JSON，适合落盘的存档文件。
     */
    public static String writePretty(Object value) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /**
     * 读取 JSON，失败返回 null。
     */
    public static <T> T read(String json, Class<T> type) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            return null;
        }
    }
}
