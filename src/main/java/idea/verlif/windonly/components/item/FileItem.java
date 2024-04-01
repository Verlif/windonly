package idea.verlif.windonly.components.item;

import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.utils.FileTypeUtil;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileItem extends VBox implements Item<List<File>> {

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
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(WindonlyConfig.getInstance().getFontSize() / 2);
        int fileNumber = WindonlyConfig.getInstance().getDisplayFileNumber();
        FileOne[] showList;
        if (fileNumber > 0 && files.length > fileNumber) {
            showList = Arrays.copyOf(files, 10);
        } else {
            showList = files;
        }
        // 对单个图片文件做显示优化
        if (showList.length == 1 && FileTypeUtil.isImage(showList[0].getFile())) {
            showList[0].setImageSize(WindonlyConfig.getInstance().getImageSize());
            showList[0].setHorizontal(false);
        }
        // 添加文件项目
        for (FileOne file : showList) {
            file.init();
        }
        ObservableList<Node> children = getChildren();
        children.addAll(showList);
        if (fileNumber > 0 && files.length > fileNumber) {
            children.addAll(createMoreTip(fileNumber));
        }
    }

    private Node createMoreTip(int standard) {
        Label label = new Label("......");
        label.setAlignment(Pos.CENTER);
        label.setFont(new Font(WindonlyConfig.getInstance().getFontSize()));
        // 设置提示
        label.setTooltip(new Tooltip(files.length - standard + "+"));
        return label;
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
        if (files.size() != this.files.length) {
            return false;
        }
        return files.stream().allMatch(file -> Arrays.stream(this.files).anyMatch(fileOne -> fileOne.getFile().equals(file)));
    }

    @Override
    public void refresh() {
        for (FileOne file : files) {
            file.refresh();
        }
    }
}
