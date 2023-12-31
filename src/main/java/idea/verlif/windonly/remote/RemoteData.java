package idea.verlif.windonly.remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import idea.verlif.windonly.WindonlyException;

import java.io.Serializable;

public class RemoteData implements Serializable {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public enum Type {
        /**
         * 连接申请
         */
        CONNECT,
        /**
         * 连接成功
         */
        CONNECTED,
        /**
         * 数据同步
         */
        SYNC,
        /**
         * 新增数据项
         */
        INSERT,
        /**
         * 删除
         */
        DELETE,
        /**
         * 申请从此客户端下载文件
         */
        DOWNLOAD,
        /**
         * 回应文件下载申请，data中包括
         */
        UPLOAD,
    }

    private String key;

    private Type type;

    private String data;

    public RemoteData(String key, Type type) {
        this.key = key;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public static RemoteData parse(String s) {
        try {
            return OBJECT_MAPPER.readValue(s, RemoteData.class);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }

    @Override
    public String toString() {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new WindonlyException(e);
        }
    }
}
