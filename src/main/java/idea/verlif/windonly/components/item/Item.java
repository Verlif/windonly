package idea.verlif.windonly.components.item;

import java.io.Serializable;

public interface Item<T> extends Serializable {

    void init();

    T getSource();

    boolean match(String key);

    boolean sourceEquals(T t);
}
