package idea.verlif.windonly.data;

public interface Savable<T> {

    T save();

    void load(T t);
}
