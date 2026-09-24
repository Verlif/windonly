package idea.verlif.windonly.utils;

import javafx.application.Platform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轻量的后台任务执行器。
 * <p>
 * 只用于网络取标题这类"可以失败、可以丢弃"的辅助任务：
 * <ul>
 *     <li>线程数受控且使用非核心线程，空闲 30 秒即回收，不会常驻内存；</li>
 *     <li>队列有界，任务堆积时直接丢弃而不是无限占用内存；</li>
 *     <li>任务异常被吞掉，不会因为一次网络失败影响线程池。</li>
 * </ul>
 *
 * @author Verlif
 */
public class ScheduledUtil {

    /**
     * 队列上限，避免大量请求把内存撑爆
     */
    private static final int QUEUE_SIZE = 32;

    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(
            0, 2,
            30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_SIZE),
            new ScheduledUtil.DaemonThreadFactory(),
            new ThreadPoolExecutor.DiscardPolicy());

    public static void execute(Runnable runnable, Runnable update) {
        if (runnable == null) {
            return;
        }
        execute(update == null ? runnable : new CheckRunnable(runnable, update));
    }

    public static void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        try {
            EXECUTOR_SERVICE.execute(() -> {
                try {
                    runnable.run();
                } catch (Throwable ignored) {
                    // 后台辅助任务失败不应该影响主流程
                }
            });
        } catch (Throwable ignored) {
            // 队列已满等情况直接放弃
        }
    }

    private record CheckRunnable(Runnable task, Runnable next) implements Runnable {

        @Override
        public void run() {
            task.run();
            Platform.runLater(next);
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {

        private final AtomicInteger index = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "windonly-helper-" + index.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
