package idea.verlif.windonly.remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.components.RemoteProjectItem;

public class RemoteItemData {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        try {
            return OBJECT_MAPPER.readValue(s, RemoteItemData.class);
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
