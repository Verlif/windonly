package idea.verlif.windonly.manage.inner;

import idea.verlif.windonly.manage.HandlerManager;

import java.io.Serializable;

public class Message implements Serializable {

    public interface What {
        /**
         * 保存存档
         */
        int ARCHIVE_SAVE = 0;

        /**
         * 复制数据列表聚焦项
         */
        int COPY = 1000;
        /**
         * 复制远程数据列表聚焦项
         */
        int COPY_REMOTE = 1010;
        /**
         * 置顶数据列表聚焦项
         */
        int SET_TO_TOP = 1001;
        /**
         * 删除数据列表聚焦项
         */
        int DELETE = 1002;
        /**
         * 删除远程数据列表聚焦项
         */
        int DELETE_REMOTE = 1012;
        /**
         * 新增远程数据
         */
        int INSERT_REMOTE = 1013;
        /**
         * 同步远程数据
         */
        int SYNC_REMOTE = 1015;

        /**
         * 置顶应用窗口
         */
        int WINDOW_PIN = 2001;
        /**
         * 窗口侧边滑出
         */
        int WINDOW_SLIDE_OUT = 2002;
        /**
         * 窗口侧边隐藏
         */
        int WINDOW_SLIDE_IN = 2003;
        /**
         * 窗口开启或关闭侧边隐藏
         */
        int WINDOW_SLIDE = 2004;
        /**
         * 窗口最小化
         */
        int WINDOW_MIN = 2005;
        /**
         * 窗口最大化
         */
        int WINDOW_MAX = 2006;
        /**
         * 关闭窗口
         */
        int WINDOW_CLOSE = 2007;

        /**
         * 锁定当前工作区
         */
        int ARCHIVE_LOCK = 3002;
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
