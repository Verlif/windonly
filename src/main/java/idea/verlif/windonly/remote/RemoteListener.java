package idea.verlif.windonly.remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import idea.verlif.socketpoint.EndPoint;
import idea.verlif.socketpoint.listener.MessageListener;
import idea.verlif.windonly.WindonlyException;
import idea.verlif.windonly.manage.inner.Message;

public class RemoteListener implements MessageListener {

    private final ObjectMapper objectMapper;

    public RemoteListener() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void receive(EndPoint endPoint, String s) {
        if (s != null && !s.isEmpty()) {
            RemoteData remoteData;
            try {
                remoteData = objectMapper.readValue(s, RemoteData.class);
            } catch (JsonProcessingException e) {
                throw new WindonlyException(e);
            }
            switch (remoteData.getType()) {
                case INSERT -> {
                }
                case TOP -> {
                }
                case DELETE -> {

                }
                case DOWNLOAD -> {

                }
            }
        }
    }

}
