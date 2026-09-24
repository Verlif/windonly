package idea.verlif.windonly.remote;

import idea.verlif.windonly.utils.JsonUtil;

import java.io.Serializable;

public class RemoteData implements Serializable {

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
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return JsonUtil.mapper().readValue(s, RemoteData.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public String toString() {
        // 网络消息使用紧凑 JSON：省掉缩进带来的额外字符串与带宽
        return JsonUtil.write(this);
    }
}
