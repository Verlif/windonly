package idea.verlif.windonly.manage.inner;

import idea.verlif.windonly.manage.HandlerManager;

import java.io.Serializable;

public abstract class Handler implements Serializable {

    public Handler(String owner) {
        tag = owner;
        HandlerManager.getInstance().addHandler(this);
    }

    public Handler() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String[] names = stack[3].getClassName().split("\\.");
        owner = names[names.length - 1];
        tag = owner;
        HandlerManager.getInstance().addHandler(this);
    }

    private String owner;
    private String tag;

    public abstract void handlerMessage(Message message);

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
