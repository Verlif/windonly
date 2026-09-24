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
import java.util.ArrayList;
import java.util.List;

public class FileItem extends VBox implements Item<List<File>> {

    private final FileOne[] files;
    /**
     * 缓存文件列表。原实现每次 getSource() 都用 Stream 重新收集一遍，
     * 而 getSource() 在保存、比对、右键菜单里会被反复调用。
     */
    private List<File> sourceCache;

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
        boolean overSize = fileNumber > 0 && files.length > fileNumber;
        if (overSize) {
            showList = new FileOne[fileNumber];
            System.arraycopy(files, 0, showList, 0, fileNumber);
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
        children.clear();
        children.addAll(showList);
        if (overSize) {
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
        List<File> cache = sourceCache;
        if (cache == null) {
            cache = new ArrayList<>(files.length);
            for (FileOne file : files) {
                cache.add(file.getFile());
            }
            sourceCache = cache;
        }
        return cache;
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
        if (files == null || files.size() != this.files.length) {
            return false;
        }
        // 与原先的 Stream 实现语义一致（无关于顺序），但不产生任何中间对象
        for (File file : files) {
            boolean found = false;
            for (FileOne one : this.files) {
                if (one.getFile().equals(file)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void refresh() {
        for (FileOne file : files) {
            file.refresh();
        }
    }
}
