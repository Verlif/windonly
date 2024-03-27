package idea.verlif.windonly.remote;

import idea.verlif.socketpoint.EndPoint;
import idea.verlif.socketpoint.SocketConfig;
import idea.verlif.socketpoint.SocketPoint;
import idea.verlif.socketpoint.listener.ClosedListener;
import idea.verlif.socketpoint.listener.ConnectedListener;
import idea.verlif.socketpoint.listener.MessageListener;
import idea.verlif.windonly.config.RemoteConfig;
import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.IpUtil;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteSocket extends SocketPoint {

    private final Map<EndPoint, String> endPointMap;
    private final Map<EndPoint, Integer> errorCount;

    public RemoteSocket() {
        endPointMap = new ConcurrentHashMap<>();
        errorCount = new ConcurrentHashMap<>();
    }

    @Override
    public void start(SocketConfig config) throws IOException {
        setConnectedListener(new PassConnectedListener());
        setMessageListener(new MessageHandler());
        super.start(config);
        registerRemoteHandler();
    }

    public String getKey() {
        return IpUtil.getLocalIp() + "." + RemoteConfig.getInstance().getPort();
    }

    private void error(EndPoint endPoint) {
        Integer count = errorCount.get(endPoint);
        if (count == null) {
            count = 0;
        }
        count++;
        if (count > 3) {
            remove(endPoint);
        } else {
            errorCount.put(endPoint, count);
        }
    }

    private void remove(String key) {
        EndPoint endPoint = null;
        for (Map.Entry<EndPoint, String> pointEntry : endPointMap.entrySet()) {
            if (pointEntry.getValue().equals(key)) {
                endPoint = pointEntry.getKey();
            }
        }
        remove(endPoint);
    }

    private void remove(EndPoint endPoint) {
        errorCount.remove(endPoint);
        endPointMap.remove(endPoint);
    }

    private void send(String message) {
        for (EndPoint endPoint : endPointMap.keySet()) {
            endPoint.send(message);
        }
    }

    private void add(EndPoint endPoint, String key) {
        endPointMap.put(endPoint, key);
    }

    private boolean check(EndPoint endPoint) {
        return endPointMap.containsKey(endPoint);
    }

    private void registerRemoteHandler() {
        new Handler() {
            @Override
            public void handlerMessage(Message message) {
                switch (message.what) {
                    case Message.What.DELETE_REMOTE: {
                        RemoteItemData remoteItemData = (RemoteItemData) message.getObj();
                        RemoteData remoteData = new RemoteData(getKey(), RemoteData.Type.DELETE);
                        remoteData.setData(remoteItemData.toString());
                        send(remoteData.toString());
                    }
                    break;
                    case Message.What.INSERT_REMOTE: {
                        RemoteItemData remoteItemData = (RemoteItemData) message.getObj();
                        RemoteData remoteData = new RemoteData(getKey(), RemoteData.Type.INSERT);
                        remoteData.setData(remoteItemData.toString());
                        send(remoteData.toString());
                    }
                    break;
                }
            }
        };
    }

    private static final class PassConnectedListener implements ConnectedListener {

        @Override
        public void onConnected(EndPoint endPoint) {
        }
    }

    private final class DisconnectedHandler implements ClosedListener {

        @Override
        public void onClosed(EndPoint endPoint) {
            remove(endPoint);
        }
    }

    private final class MessageHandler implements MessageListener {

        @Override
        public void receive(EndPoint endPoint, String s) {
            RemoteData data = RemoteData.parse(s);
            if (data != null) {
                switch (data.getType()) {
                    case CONNECT: {
                        RemoteData returnData = new RemoteData(getKey(), RemoteData.Type.CONNECTED);
                        endPoint.send(returnData.toString());
                    }
                    break;
                    case CONNECTED: {
                        add(endPoint, data.getKey());
                    }
                    break;
                    case SYNC: {
                        if (check(endPoint)) {
                            RemoteItemData itemData = RemoteItemData.parse(data.getData());
                            if (itemData != null) {
                                new Message(Message.What.SYNC_REMOTE).send(itemData);
                            }
                        }
                    }
                    break;
                    case INSERT: {
                        if (check(endPoint)) {
                            RemoteItemData itemData = RemoteItemData.parse(data.getData());
                            if (itemData != null) {
                                new Message(Message.What.INSERT_REMOTE).send(itemData);
                            }
                        }
                    }
                    break;
                    case DELETE: {
                        if (check(endPoint)) {
                            RemoteItemData itemData = RemoteItemData.parse(data.getData());
                            if (itemData != null) {
                                new Message(Message.What.DELETE_REMOTE).send(itemData);
                            }
                        }
                    }
                    break;
                    case DOWNLOAD:
                    case UPLOAD: {
                        if (check(endPoint)) {
                            RemoteItemData itemData = RemoteItemData.parse(data.getData());
                            if (itemData != null) {

                            }
                        }
                    }
                    break;
                    default: {
                        error(endPoint);
                    }
                }
            }
        }
    }
}
