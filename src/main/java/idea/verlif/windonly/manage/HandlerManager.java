package idea.verlif.windonly.manage;

import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import javafx.application.Platform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息分发中心。
 * <p>
 * 原实现把每条消息都丢进一个 4 线程 + 99 长度队列的线程池：
 * <ul>
 *     <li>消息体几乎都是 UI 操作，真正的 UI 修改仍然要回到 FX 线程，多一次线程切换纯属浪费；</li>
 *     <li>消息量稍大（例如拖动窗口宽度、快速增删数据）就会触发队列拒绝，
 *         直接把 {@code RejectedExecutionException} 抛到 JavaFX 事件处理里；</li>
 *     <li>4 个常驻线程本身也是额外开销，而且顺序无法保证。</li>
 * </ul>
 * 现在改为：<b>已经在 FX 线程就同步分发，否则用 {@code Platform.runLater} 排到 FX 线程</b>。
 * 这样既保证了处理逻辑总在 FX 线程执行（原实现里有直接操作 Stage 的处理器，多线程调用是不安全的），
 * 也保证了消息顺序，同时省掉了线程池与队列。
 */
public class HandlerManager {

    private static final HandlerManager INSTANCE = new HandlerManager();

    private final Map<String, Handler> handlerHashMap = new ConcurrentHashMap<>();

    private HandlerManager() {
    }

    public static HandlerManager getInstance() {
        return INSTANCE;
    }

    public void addHandler(Handler handler) {
        if (handler != null && handler.getOwner() != null) {
            // 同一个类只保留最新注册的处理器，避免重复注册后同一条消息被处理多次
            handlerHashMap.put(handler.getOwner(), handler);
        }
    }

    public void removeHandler(Handler handler) {
        if (handler != null && handler.getOwner() != null) {
            handlerHashMap.remove(handler.getOwner(), handler);
        }
    }

    /**
     * 将Message交由相关的Handler处理。
     *
     * @param message 需要处理的Handler
     */
    public void handlerMessage(Message message) {
        if (message == null) {
            return;
        }
        if (isFxThread()) {
            dispatch(message);
        } else {
            try {
                Platform.runLater(() -> dispatch(message));
            } catch (Throwable t) {
                // FX 线程已经结束时退化为直接执行
                dispatch(message);
            }
        }
    }

    private void dispatch(Message message) {
        String tag = message.getTag();
        if (tag != null && !tag.isEmpty()) {
            Handler handler = handlerHashMap.get(tag);
            if (handler != null) {
                handler.handlerMessage(message);
            }
            return;
        }
        for (Handler handler : handlerHashMap.values()) {
            try {
                handler.handlerMessage(message);
            } catch (Throwable ignored) {
                // 单个处理器异常不应该影响其它处理器
            }
        }
    }

    private static boolean isFxThread() {
        try {
            return Platform.isFxApplicationThread();
        } catch (Throwable t) {
            return false;
        }
    }
}
