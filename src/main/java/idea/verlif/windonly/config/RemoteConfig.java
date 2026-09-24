package idea.verlif.windonly.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.data.Archive;
import idea.verlif.windonly.data.Savable;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.JsonUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        if (this.port == port) {
            return;
        }
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
        if (Objects.equals(this.storagePath, storagePath)) {
            return;
        }
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
        if (this.enabled == enabled) {
            return;
        }
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
        saveToFile();
        // 原实现只是 new 了一个 Message，没有 send，等于什么都没发生
        new Message(Message.What.SYNC_REMOTE).send();
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
        return JsonUtil.writePretty(this);
    }

    @Override
    public void load(String s) {
        if (s != null && !s.isEmpty()) {
            try {
                JsonNode remoteConfig = JsonUtil.mapper().reader().readTree(s);
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
                        // 原实现对数组节点调用 asText() 得到空串，解析必然失败，
                        // 而且随后还会用空串覆盖 storagePath。这里直接用 convertValue 读取数组。
                        List<IpData> ips = JsonUtil.mapper().convertValue(ipNode, new TypeReference<List<IpData>>() {
                        });
                        if (ips != null) {
                            ipData.addAll(ips);
                        }
                    }
                }
            } catch (Exception e) {
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
