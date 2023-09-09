package idea.verlif.windonly.data;

import java.io.Serializable;

public interface Savable<T> extends Serializable {

    T save();

    void load(T t);
}
