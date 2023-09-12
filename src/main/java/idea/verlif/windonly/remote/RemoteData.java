package idea.verlif.windonly.remote;

import idea.verlif.windonly.components.ProjectItem;

public class RemoteData {

    public enum Type {
        /**
         * 新增数据项
         */
        INSERT,
        /**
         * 置顶
         */
        TOP,
        /**
         * 删除
         */
        DELETE,
        /**
         * 文件下载完毕
         */
        DOWNLOAD,
    }

    private String key;

    private Type type;

    private String data;

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
}
