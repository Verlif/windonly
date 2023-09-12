package idea.verlif.windonly.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.data.Savable;

public class RemoteConfig implements Savable<String> {

    private static final RemoteConfig REMOTE_CONFIG = new RemoteConfig();

    private int port = 1725;
    private String storagePath = "remote";

    private RemoteConfig() {}

    public static RemoteConfig getInstance() {
        return REMOTE_CONFIG;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public String save() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(this);
        } catch (JsonProcessingException ignored) {
            return "";
        }
    }

    @Override
    public void load(String s) {
        if (s != null && !s.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode remoteConfig = mapper.reader().readTree(s);
                if (remoteConfig.has("port")) {
                    setPort(remoteConfig.get("port").asInt());
                }
                if (remoteConfig.has("storagePath")) {
                    setStoragePath(remoteConfig.get("storagePath").asText());
                }
            } catch (JsonProcessingException e) {
                throw new WindonlyException(e);
            }
        }
    }
}
