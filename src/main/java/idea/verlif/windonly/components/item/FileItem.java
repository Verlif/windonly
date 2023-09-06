package idea.verlif.windonly.components.item;

import idea.verlif.windonly.config.WindonlyConfig;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.layout.HBox;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileItem extends HBox implements Item<List<File>> {

    private final FileOne[] files;

    public FileItem(List<File> files) {
        this.files = new FileOne[files.size()];
        for (int i = 0; i < this.files.length; i++) {
            this.files[i] = new FileOne(files.get(i));
        }
    }

    public FileItem(File file) {
        this.files = new FileOne[1];
        this.files[0] = new FileOne(file);
    }

    @Override
    public void init() {
        setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        setHeight(WindonlyConfig.getInstance().getImageSize() + 10);
        setSpacing(WindonlyConfig.getInstance().getFontSize() / 2);
        // 添加文件项目
        for (FileOne file : files) {
            file.init();
        }
        getChildren().addAll(files);
    }

    @Override
    public List<File> getSource() {
        return Arrays.stream(files).map(FileOne::getFile).collect(Collectors.toList());
    }

    @Override
    public boolean match(String key) {
        for (FileOne file : files) {
            if (file.match(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean sourceEquals(List<File> files) {
        if (!files.isEmpty()) {
            File file = files.get(0);
            for (FileOne f : this.files) {
                if (!f.sourceEquals(file)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
