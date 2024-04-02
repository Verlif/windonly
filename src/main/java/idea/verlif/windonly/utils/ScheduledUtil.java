package idea.verlif.windonly.utils;

import javafx.application.Platform;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ScheduledUtil {

    private static final ScheduledExecutorService EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(2);

    public static void execute(Runnable runnable, Runnable update) {
        if (update != null) {
            EXECUTOR_SERVICE.execute(new CheckRunnable(runnable, update));
        } else {
            EXECUTOR_SERVICE.execute(runnable);
        }
    }

    private record CheckRunnable(Runnable task, Runnable next) implements Runnable {

        @Override
        public void run() {
            task.run();
            Platform.runLater(next);
        }
    }
}
