package idea.verlif.windonly.utils;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class ScreenUtil {

    /**
     * 获取屏幕大小
     * @return 0号位是宽度，1号位是高度
     */
    public static double[] getScreenSize() {
        Rectangle2D screenRectangle = Screen.getPrimary().getBounds();
        double width = screenRectangle.getWidth();
        double height = screenRectangle.getHeight();
        return new double[]{width, height};
    }
}
