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
         * 刷新数据列表
         */
        int DATA_REFRESH = 1;

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
         * 编辑数据列表聚焦项
         */
        int EDIT = 1003;
        /**
         * 从系统打开
         */
        int OPEN_WITH_SYSTEM = 1004;
        /**
         * 打开文件位置
         */
        int OPEN_WITH_EXPLORE = 1005;
        /**
         * 从浏览器打开
         */
        int OPEN_WITH_BROWSE = 1006;
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
         * 显示tip
         */
        int TIP = 1099;

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
         * 窗口聚焦
         */
        int WINDOW_FOCUS = 2008;
        /**
         * 窗口失焦
         */
        int WINDOW_NOT_FOCUS = 2009;
        /**
         * 窗口请求侧边隐藏
         */
        int WINDOW_REQUIRE_HIDDEN = 2010;
        /**
         * 窗口宽度更改
         */
        int WINDOW_CHANGED_WIDTH = 2101;
        /**
         * 锁定当前工作区
         */
        int ARCHIVE_LOCK = 3002;
        /**
         * 缩放设置更新
         */
        int SETTING_MAGNIFICATION = 4001;
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
