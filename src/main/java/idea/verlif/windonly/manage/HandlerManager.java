package idea.verlif.windonly.manage;

import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HandlerManager {

    private static class HandlerHandler {
        private static final HandlerManager INSTANCE = new HandlerManager();
    }

    private final HashMap<String, Handler> handlerHashMap;
    private final ThreadPoolExecutor executor;

    private HandlerManager() {
        handlerHashMap = new HashMap<>();
        executor = new ThreadPoolExecutor(
                4, 4,
                1, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(99),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    return thread;
                });
    }

    public static HandlerManager getInstance() {
        return HandlerHandler.INSTANCE;
    }

    public void addHandler(Handler handler) {
        handlerHashMap.put(handler.getOwner(), handler);
    }

    public void removeHandler(Handler handler) {
        handlerHashMap.remove(handler.getTag());
    }

    /**
     * 将Message交由相关的Handler处理。
     *
     * @param message 需要处理的Handler
     */
    public void handlerMessage(Message message) {
        executor.execute(() -> {
            String tag = message.getTag();
            // 指定接收对象
            if (tag != null && !tag.isEmpty()) {
                synchronized (handlerHashMap) {
                    for (Handler handler : handlerHashMap.values()) {
                        if (handler.getTag().equals(tag)) {
                            handler.handlerMessage(message);
                        }
                    }
                }
            } else {
                synchronized (handlerHashMap) {
                    for (Handler handler : handlerHashMap.values()) {
                        handler.handlerMessage(message);
                    }
                }
            }
        });
    }

}
