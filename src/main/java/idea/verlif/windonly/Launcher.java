package idea.verlif.windonly;

/**
 * 打包发行时使用的启动入口。
 * <p>
 * 如果直接把 {@link WindonlyApplication}（{@code javafx.application.Application} 的子类）
 * 作为主类，JDK 的启动器会强制要求 JavaFX 必须位于模块路径上，否则会直接报
 * “缺少 JavaFX 运行时组件”。而在免安装发行包里，JavaFX 是以普通 jar 的形式随应用一起分发的，
 * 用这个不继承 {@code Application} 的类作为入口即可绕开该限制。
 * <p>
 * 使用模块化方式运行（例如 IDE 或 {@code mvn javafx:run}）时依然可以直接使用
 * {@code WindonlyApplication}。
 *
 * @author Verlif
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        WindonlyApplication.main(args);
    }
}
