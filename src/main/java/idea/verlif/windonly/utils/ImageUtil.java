package idea.verlif.windonly.utils;

import javafx.scene.image.Image;
import javafx.stage.Screen;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.net.URI;
import java.util.Iterator;

/**
 * 图片加载工具。
 * <p>
 * JavaFX 的 {@link Image} 会把解码后的像素完整保留在堆外内存里，且 {@code ImageView} 的
 * fitWidth / fitHeight <b>只是渲染时缩放</b>，并不会减少 {@code Image} 自身的内存占用。
 * 列表里的缩略图高度通常只有 16 ~ 64 像素，但原实现直接 {@code new Image(path)}，
 * 一张 1MB 的 6000x4000 照片就会占用约 96MB 的像素缓冲，几十条数据就足以把内存吃满。
 * <p>
 * 这里在解码前先读取图片文件头拿到原始尺寸，只按显示需要的大小解码，
 * 从根源上避免"为了画 32px 而解码整张原图"。
 *
 * @author Verlif
 */
public final class ImageUtil {

    /**
     * 最小解码高度，避免过小的目标尺寸导致图片糊成一团
     */
    private static final double MIN_DECODE_HEIGHT = 32;

    private static volatile double uiScale = -1;

    private ImageUtil() {
    }

    /**
     * 当前屏幕的缩放比例（高分屏下需要解出更多像素才清晰）
     */
    public static double getUiScale() {
        double scale = uiScale;
        if (scale > 0) {
            return scale;
        }
        try {
            scale = Math.max(1, Screen.getPrimary().getOutputScaleX());
        } catch (Throwable t) {
            scale = 1;
        }
        uiScale = scale;
        return scale;
    }

    /**
     * 读取图片的原始像素尺寸。只解析文件头，不解码像素数据，开销很小。
     *
     * @return 长度为 2 的数组 [宽, 高]，无法读取时返回 null
     */
    public static int[] readDimension(File file) {
        if (file == null || !file.isFile()) {
            return null;
        }
        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            if (input == null) {
                return null;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            while (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(input);
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    if (width > 0 && height > 0) {
                        return new int[]{width, height};
                    }
                } finally {
                    reader.dispose();
                }
            }
        } catch (Throwable ignored) {
            // 交给调用方回退到常规加载方式
        }
        return null;
    }

    /**
     * 加载用于列表显示的图片。若原始图片远大于显示尺寸，则按显示尺寸解码。
     *
     * @param url          图片地址（本地路径或 URL）
     * @param targetHeight 目标显示高度（像素）
     */
    public static Image loadForDisplay(String url, double targetHeight) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        double height = Math.max(MIN_DECODE_HEIGHT, targetHeight * getUiScale());
        File file = toFile(url);
        if (file != null) {
            int[] dimension = readDimension(file);
            if (dimension != null) {
                if (dimension[1] <= height) {
                    // 原图本来就很小，直接加载
                    return new Image(url);
                }
                double scale = height / dimension[1];
                int width = Math.max(1, (int) Math.round(dimension[0] * scale));
                return new Image(url, width, (int) Math.round(height), true, true, false);
            }
        }
        // 无法预读尺寸（例如网络图片）时，交给 JavaFX 按请求尺寸解码
        return new Image(url, 0, (int) Math.round(height), true, true, false);
    }

    /**
     * 将图片地址转换为本地文件，非本地文件返回 null。
     */
    public static File toFile(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            if (url.regionMatches(true, 0, "file:", 0, 5)) {
                File file = new File(URI.create(url));
                return file.isFile() ? file : null;
            }
        } catch (Throwable ignored) {
        }
        File file = new File(url);
        return file.isFile() ? file : null;
    }
}
