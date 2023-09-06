package idea.verlif.windonly.utils;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.File;
import java.util.List;

public class ClipboardUtil {

    public static void copyToSystemClipboard(Object target) {
        ClipboardContent content = new ClipboardContent();
        if (target instanceof List) {
            content.putFiles((List<File>) target);
        } else if (target instanceof File) {
            content.putFiles(List.of((File) target));
        } else if (target instanceof Image) {
            content.putImage((Image) target);
        } else {
            String s = target.toString();
            content.putString(s);
        }
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            clipboard.setContent(content);
        });
    }
}
