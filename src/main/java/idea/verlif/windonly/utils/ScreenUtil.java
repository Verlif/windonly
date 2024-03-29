package idea.verlif.windonly.utils;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ScreenUtil {

    /**
     * 获取屏幕大小
     * @return 0号位是宽度，1号位是高度
     */
    public static double[] getScreenSize(Stage stage) {
        Rectangle2D screenRectangle = getNowScreen(stage).getBounds();
        double width = screenRectangle.getWidth();
        double height = screenRectangle.getHeight();
        return new double[]{width, height};
    }

    public static Screen getNowScreen(Stage stage) {
        // 获取窗口的屏幕坐标
        double windowLeft = stage.getX();
        double windowRight = windowLeft + stage.getWidth();
        double windowTop = stage.getY();
        double windowBottom = stage.getY() + stage.getHeight();
        double[][] xys = {{windowLeft, windowTop}, {windowRight, windowTop}, {windowLeft, windowBottom}, {windowRight, windowBottom}};
        // 获取所有屏幕
        ObservableList<Screen> screens = Screen.getScreens();
        // 遍历屏幕并检查窗口位置
        for (double[] xy : xys) {
            for (Screen screen : screens) {
                Rectangle2D bounds = screen.getBounds();
                if (bounds.contains(xy[0], xy[1])) {
                    return screen;
                }
            }
        }
        return Screen.getPrimary();
    }
}
