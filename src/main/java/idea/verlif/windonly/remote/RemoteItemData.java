package idea.verlif.windonly.remote;

import idea.verlif.windonly.components.RemoteProjectItem;
import idea.verlif.windonly.utils.JsonUtil;

public class RemoteItemData {

    private static int staticKey = 0;
    private String key;

    private RemoteProjectItem.Type type;

    private String data;

    public RemoteItemData() {
        key = String.valueOf(staticKey++);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public RemoteProjectItem.Type getType() {
        return type;
    }

    public void setType(RemoteProjectItem.Type type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public static RemoteItemData parse(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return JsonUtil.mapper().readValue(s, RemoteItemData.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public String toString() {
        return JsonUtil.write(this);
    }
}
