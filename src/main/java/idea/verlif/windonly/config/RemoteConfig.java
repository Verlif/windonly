package idea.verlif.windonly.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import idea.verlif.socketpoint.SocketConfig;
import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.data.Archive;
import idea.verlif.windonly.data.Savable;
import idea.verlif.windonly.manage.inner.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RemoteConfig implements Savable<String> {

    private static final RemoteConfig REMOTE_CONFIG = new RemoteConfig();

    private int port = 1725;
    private String storagePath = "remote";
    private boolean enabled;
    private final List<IpData> ipData;

    private RemoteConfig() {
        ipData = new ArrayList<>();
    }

    public static RemoteConfig getInstance() {
        return REMOTE_CONFIG;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        initPort(port);
        saveToFile();
    }

    public void initPort(int port) {
        this.port = port;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        initStoragePath(storagePath);
        saveToFile();
    }

    public void initStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        initEnabled(enabled);
        saveToFile();
    }

    public void initEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<IpData> getIpData() {
        return ipData;
    }

    public void addIpData(IpData ipData) {
        this.ipData.add(ipData);
        new Message(Message.What.SYNC_REMOTE);
    }

    public void removeIpData(IpData ipData) {
        this.ipData.remove(ipData);
    }

    public void saveToFile() {
        Archive archive = new Archive(Archive.getCurrentArchive());
        archive.save(RemoteConfig.this);
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
                    initPort(remoteConfig.get("port").asInt());
                }
                if (remoteConfig.has("storagePath")) {
                    initStoragePath(remoteConfig.get("storagePath").asText());
                }
                if (remoteConfig.has("enabled")) {
                    initEnabled(remoteConfig.get("enabled").asBoolean());
                }
                ipData.clear();
                if (remoteConfig.has("ipData")) {
                    JsonNode ipNode = remoteConfig.get("ipData");
                    if (ipNode.isArray()) {
                        TypeReference<List<IpData>> tRef = new TypeReference<>() {};
                        List<IpData> ips = mapper.readValue(ipNode.asText(), tRef);
                        ipData.addAll(ips);
                    }
                    setStoragePath(ipNode.asText());
                }
            } catch (JsonProcessingException e) {
                throw new WindonlyException(e);
            }
        }
    }

    public static final class IpData implements Serializable {

        private String ip;

        private int port;

        private boolean enabled;

        public IpData() {
        }

        public IpData(String ip) {
            String[] split = ip.split(":");
            this.ip = split[0];
            if (split.length > 1) {
                this.port = Integer.parseInt(split[1]);
            }
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
