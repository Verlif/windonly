package idea.verlif.windonly.manage.inner;

import idea.verlif.windonly.manage.HandlerManager;

import java.io.Serializable;

public class Message implements Serializable {

    public interface What {
        int COPY = 1000;
        int SET_TO_TOP = 1001;
        int DELETE = 1002;
        int QUICK_PASTE = 1003;

        int WINDOW_PIN = 2001;
        int ARCHIVE_LOCK = 2002;
    }

    /**
     * 消息标签
     */
    private String tag;
    /**
     * 消息类型
     */
    public int what;
    /**
     * 消息附加实例
     */
    public Object obj;

    public Message() {
    }

    public Message(int what) {
        this.what = what;
    }

    public Message(Class<?> target) {
        target(target);
    }

    /**
     * 消息接收类
     *
     * @param target 期望此消息的接受对象类
     */
    public void target(Class<?> target) {
        this.tag = target.getSimpleName();
    }

    public void send() {
        HandlerManager.getInstance().handlerMessage(this);
    }

    public void send(Object target) {
        this.obj = target;
        HandlerManager.getInstance().handlerMessage(this);
    }

    public String getTag() {
        return tag;
    }

    public int getWhat() {
        return what;
    }

    public Object getObj() {
        return obj;
    }
}
