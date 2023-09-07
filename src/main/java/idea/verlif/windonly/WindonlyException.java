package idea.verlif.windonly;

public class WindonlyException extends RuntimeException {
    public WindonlyException() {
    }

    public WindonlyException(String message) {
        super(message);
    }

    public WindonlyException(String message, Throwable cause) {
        super(message, cause);
    }

    public WindonlyException(Throwable cause) {
        super(cause);
    }

    public WindonlyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
