package idea.verlif.windonly.components.item;

import java.io.Serializable;

public interface Item<T> extends Serializable {

    void init();

    T getSource();

    default void setSource(T t) {}

    boolean match(String key);

    boolean sourceEquals(T t);

    void refresh();
}
