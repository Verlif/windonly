package idea.verlif.windonly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import idea.verlif.windonly.components.ProjectItem;
import idea.verlif.windonly.components.alert.ConfirmAlert;
import idea.verlif.windonly.components.alert.InputAlert;
import idea.verlif.windonly.components.item.FileItem;
import idea.verlif.windonly.components.item.ImageOne;
import idea.verlif.windonly.components.item.TextItem;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.data.Archive;
import idea.verlif.windonly.data.Savable;
import idea.verlif.windonly.manage.HandlerManager;
import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.ClipboardUtil;
import idea.verlif.windonly.utils.MessageUtil;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WindonlyController implements Initializable, Serializable {

    public TextField input;
    public ListView<ProjectItem> list;
    public ImageView lockView;
    public ImageView pinView;
    public BorderPane center;
    public ChoiceBox<String> archiveBox;

    private final ProjectItemManager projectItemManager;
    private Archive nowArchive;

    public WindonlyController() {
        projectItemManager = new ProjectItemManager();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        input.setPrefHeight(WindonlyConfig.getInstance().getFontSize() + 8);
        input.setFont(new Font(WindonlyConfig.getInstance().getFontSize()));
        input.setOnDragEntered(new InputOnDrag());
        // 添加搜索监听
        input.textProperty().addListener((observableValue, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                // 为空显示所有
                projectItemManager.resetProjectItems();
            } else {
                // 有输入值时，显示过滤列表
                list.getItems().clear();
                List<ProjectItem> all = projectItemManager.getAll();
                List<ProjectItem> search = new ArrayList<>(all.size());
                for (ProjectItem item : all) {
                    if (item.match(newVal)) {
                        search.add(item);
                    }
                }
                list.getItems().addAll(search);
            }
        });
        // 添加输入事项
        input.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER && !input.getText().isEmpty()) {
                handleDragItem(input.getText());
                clearInput();
            }
        });
        // 对CtrlV特殊处理
        input.setOnKeyPressed(keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.V) {
                Platform.runLater(() -> {
                    Object o = ClipboardUtil.getFormSystemClipboard();
                    if (!(o instanceof String)) {
                        handleDragItem(o);
                    }
                });
            }
        });
        // 设置list
        list.setFocusTraversable(false);
        // 设置数据展示区拖入事件
        list.setOnDragEntered(new ListOnDrag());
        // 设置pin
        pinView.setFitHeight(WindonlyConfig.getInstance().getFontSize());
        pinView.setFitWidth(WindonlyConfig.getInstance().getFontSize());
        switchPin(WindonlyConfig.getInstance().isAlwaysShow());
        pinView.getParent().setOnMouseClicked(mouseEvent -> {
            boolean alwaysShow = WindonlyConfig.getInstance().isAlwaysShow();
            WindonlyConfig.getInstance().setAlwaysShow(!alwaysShow);
        });
        // 设置锁
        lockView.setFitHeight(pinView.getFitHeight());
        lockView.setFitWidth(pinView.getFitWidth());
        switchLock(WindonlyConfig.getInstance().isLock());
        lockView.getParent().setOnMouseClicked(mouseEvent -> {
            boolean lock = WindonlyConfig.getInstance().isLock();
            WindonlyConfig.getInstance().setLock(!lock);
        });
        // 工作区设定
        archiveBox.setPrefHeight(input.getPrefHeight());
        archiveBox.setPrefWidth(100 * WindonlyConfig.getInstance().getMagnification());
        archiveBox.setPadding(new Insets(4, 0, 5, 0));
        archiveBox.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            if (WindonlyConfig.getInstance().isLock()) {
                showTip(MessageUtil.get("notChangeArchiveWhenLocked"));
                archiveBox.setValue(Archive.getCurrentArchive());
            } else {
                load(newVal);
            }
        });
        // 初始化工作区存档
        refreshArchiveBox();
        // 增加快捷方式
        archiveBox.setContextMenu(createArchiveMenu());
        // 切换到回当前分区
        selectArchive(Archive.getCurrentArchive());

        // 注册监听
        registerHandler();
    }

    private void refreshArchiveBox() {
        archiveBox.getItems().clear();
        archiveBox.getItems().addAll(Archive.allArchives());
    }

    private void selectArchive(String archive) {
        archiveBox.setValue(archive);
    }

    private ContextMenu createArchiveMenu() {
        // 新增工作区
        MenuItem newArchive = new MenuItem(MessageUtil.get("newArchive"));
        newArchive.setOnAction(actionEvent -> {
            new InputAlert(MessageUtil.get("newArchive")) {
                @Override
                public void input(String text) {
                    if (!text.isEmpty()) {
                        Archive.newArchive(text);
                        refreshArchiveBox();
                        selectArchive(text);
                        close();
                    }
                }
            }.show();
        });
        // 修改工作区名称
        MenuItem renameArchive = new MenuItem(MessageUtil.get("renameArchive"));
        renameArchive.setOnAction(actionEvent -> {
            new InputAlert(MessageUtil.get("renameArchive")) {
                @Override
                public void input(String text) {
                    if (!text.isEmpty()) {
                        if (Archive.renameArchive(Archive.getCurrentArchive(), text)) {
                            refreshArchiveBox();
                            // 切换到回当前分区
                            selectArchive(text);
                            close();
                        }
                    }
                }
            }.show();
        });
        // 删除工作区
        MenuItem delArchive = new MenuItem(MessageUtil.get("delArchive"));
        delArchive.setOnAction(actionEvent -> {
            new ConfirmAlert(MessageUtil.get("delArchive") + " - " + Archive.getCurrentArchive()) {
                @Override
                public void confirm() {
                    Archive.delArchive(Archive.getCurrentArchive());
                    refreshArchiveBox();
                    // 切换到回当前分区
                    selectArchive(Archive.allArchives().get(0));
                }
            }.show();
        });
        // 刷新工作区列表
        MenuItem refreshArchive = new MenuItem(MessageUtil.get("refreshArchiveList"));
        refreshArchive.setOnAction(actionEvent -> {
            refreshArchiveBox();
            // 切换到回当前分区
            selectArchive(Archive.getCurrentArchive());
        });

        return new ContextMenu(newArchive, renameArchive, delArchive, refreshArchive);
    }

    private void showTip(String text) {
        Label tip = new Label(text);
        tip.setFont(new Font(WindonlyConfig.getInstance().getFontSize()));
        tip.setOnMouseClicked(mouseEvent -> closeTip());
        BorderPane pane = new BorderPane();
        pane.setCenter(tip);
        center.setBottom(pane);
    }

    private void closeTip() {
        center.setBottom(null);
    }

    /**
     * 切换窗口置顶开关
     *
     * @param pin 是否窗口置顶
     */
    private void switchPin(boolean pin) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(pin ? "/images/pin.png" : "/images/unpin.png")) {
            if (resourceAsStream != null) {
                pinView.setImage(new Image(resourceAsStream));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void switchLock(boolean lock) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(lock ? "/images/lock.png" : "/images/unlock.png")) {
            if (resourceAsStream != null) {
                lockView.setImage(new Image(resourceAsStream));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载工作区存档
     *
     * @param archive 工作区名称
     */
    private void load(String archive) {
        nowArchive = new Archive(archive);
        nowArchive.load(WindonlyConfig.getInstance());
        nowArchive.load(projectItemManager);
        Archive.setCurrentArchive(archive);
    }

    /**
     * 保存工作区信息
     */
    private void save() {
        nowArchive.save(WindonlyConfig.getInstance());
        nowArchive.save(projectItemManager);
    }

    @FXML
    protected void inputClicked() {
    }

    private void clearInput() {
        input.setText("");
    }

    /**
     * 注册需要处理的信息
     */
    private void registerHandler() {
        HandlerManager.getInstance().addHandler(new Handler() {
            @Override
            public void handlerMessage(Message message) {
                switch (message.what) {
                    case Message.What.COPY -> {
                        ProjectItem focusedItem = list.getFocusModel().getFocusedItem();
                        ClipboardUtil.copyToSystemClipboard(focusedItem.getSource());
                    }
                    case Message.What.DELETE -> {
                        ProjectItem focusedItem = list.getFocusModel().getFocusedItem();
                        Platform.runLater(() -> {
                            projectItemManager.remove(focusedItem);
                        });
                    }
                    case Message.What.SET_TO_TOP -> {
                        ProjectItem focusedItem = list.getFocusModel().getFocusedItem();
                        Platform.runLater(() -> {
                            projectItemManager.remove(focusedItem);
                            projectItemManager.add(0, focusedItem);
                        });
                    }
                    case Message.What.QUICK_PASTE -> {
                        Platform.runLater(() -> {
                            handleDragItem(message.getObj());
                        });
                    }
                    case Message.What.WINDOW_PIN -> {
                        Platform.runLater(() -> {
                            switchPin(WindonlyConfig.getInstance().isAlwaysShow());
                        });
                    }
                    case Message.What.ARCHIVE_LOCK -> {
                        Platform.runLater(() -> {
                            switchLock(WindonlyConfig.getInstance().isLock());
                        });
                    }
                }
            }
        });
    }

    /**
     * 处理拖拽进入的数据
     *
     * @param o 数据对象
     */
    private void handleDragItem(Object o) {
        // 去除重复添加
        List<ProjectItem> all = projectItemManager.getAll();
        if (!all.isEmpty() && all.stream().anyMatch(projectItem -> projectItem.sourceEquals(o))) {
            return;
        }
        ProjectItem projectItem;
        if (o instanceof List) {
            FileItem fileItem = new FileItem((List<File>) o);
            fileItem.init();
            projectItem = new ProjectItem(fileItem);
        } else if (o instanceof File) {
            FileItem fileItem = new FileItem((File) o);
            fileItem.init();
            projectItem = new ProjectItem(fileItem);
        } else if (o instanceof Image) {
            if (((Image) o).getUrl() != null) {
                ImageOne imageOne = new ImageOne((Image) o);
                imageOne.init();
                projectItem = new ProjectItem(imageOne);
            } else {
                return;
            }
        } else {
            TextItem textItem = new TextItem(o.toString());
            textItem.init();
            projectItem = new ProjectItem(textItem);
        }
        projectItemManager.add(0, projectItem);
    }

    private final class InputOnDrag implements EventHandler<DragEvent> {

        @Override
        public void handle(DragEvent dragEvent) {
            Dragboard dragboard = dragEvent.getDragboard();
            if (dragboard.hasFiles()) {
                List<File> files = dragboard.getFiles();
                StringBuilder s = new StringBuilder();
                for (File file : files) {
                    s.append(file.getName()).append(";");
                }
                input.setText(s.substring(0, s.length() - 1));
            } else if (dragboard.hasImage()) {
                Image image = dragboard.getImage();
                input.setText(image.getUrl());
            } else if (dragboard.hasString()) {
                input.setText(dragboard.getString());
            } else if (dragboard.hasUrl()) {
                input.setText(dragboard.getUrl());
            } else if (dragboard.hasHtml()) {
                input.setText(dragboard.getHtml());
            }
        }
    }

    private final class ListOnDrag implements EventHandler<DragEvent> {

        @Override
        public void handle(DragEvent dragEvent) {
            Dragboard dragboard = dragEvent.getDragboard();
            if (dragboard.hasFiles()) {
                handleDragItem(dragboard.getFiles());
            } else if (dragboard.hasImage()) {
                Image image = dragboard.getImage();
                handleDragItem(image);
            } else if (dragboard.hasString()) {
                handleDragItem(dragboard.getString());
            } else if (dragboard.hasUrl()) {
                handleDragItem(dragboard.getUrl());
            }
        }
    }

    /**
     * 项目条目管理器
     */
    private final class ProjectItemManager implements Savable<String> {

        public final List<ProjectItem> all;

        private ProjectItemManager() {
            this.all = new ArrayList<>();
        }

        public void add(ProjectItem projectItem) {
            if (checkAccess()) {
                list.getItems().add(projectItem);
                this.all.add(projectItem);
                WindonlyController.this.save();
            }
        }

        public void add(int index, ProjectItem projectItem) {
            if (checkAccess()) {
                list.getItems().add(index, projectItem);
                this.all.add(index, projectItem);
                WindonlyController.this.save();
            }
        }

        public void remove(ProjectItem projectItem) {
            if (checkAccess()) {
                list.getItems().remove(projectItem);
                this.all.remove(projectItem);
                WindonlyController.this.save();
            }
        }

        public void resetProjectItems() {
            list.getItems().clear();
            list.getItems().addAll(this.all);
            WindonlyController.this.save();
        }

        public List<ProjectItem> getAll() {
            return all;
        }

        public void clear() {
            if (checkAccess()) {
                list.getItems().clear();
                this.all.clear();
            }
        }

        private boolean checkAccess() {
            if (WindonlyConfig.getInstance().isLock()) {
                showTip(MessageUtil.get("notModifyArchiveWhenLocked"));
                return false;
            } else {
                return true;
            }
        }

        @Override
        public String save() {
            ObjectMapper mapper = new ObjectMapper();
            List<ProjectItemData> list = new ArrayList<>();
            for (ProjectItem projectItem : projectItemManager.all) {
                ProjectItemData data;
                if (projectItem.getType() == ProjectItem.Type.FILE) {
                    data = new ProjectItemData(projectItem.getType(), ((File) projectItem.getSource()).getAbsolutePath());
                } else if (projectItem.getType() == ProjectItem.Type.FILES) {
                    List<File> source = (List<File>) projectItem.getSource();
                    StringBuilder s = new StringBuilder();
                    for (File file : source) {
                        s.append(file.getAbsoluteFile()).append(",");
                    }
                    data = new ProjectItemData(projectItem.getType(), s.substring(0, s.length() - 1));
                } else if (projectItem.getType() == ProjectItem.Type.IMAGE) {
                    data = new ProjectItemData(projectItem.getType(), ((Image) projectItem.getSource()).getUrl());
                } else {
                    data = new ProjectItemData(projectItem.getType(), projectItem.getSource().toString());
                }
                list.add(data);
            }
            try {
                return mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(list);
            } catch (JsonProcessingException e) {
                return "";
            }
        }

        @Override
        public void load(String s) {
            clear();
            if (s != null && !s.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, ProjectItemData.class);
                    List<ProjectItemData> list = mapper.readValue(s, javaType);
                    // 反向装载
                    for (int i = list.size() - 1; i > -1; i--) {
                        ProjectItemData projectItem = list.get(i);
                        if (projectItem.type == ProjectItem.Type.FILE) {
                            handleDragItem(new File(projectItem.getSource()));
                        } else if (projectItem.type == ProjectItem.Type.FILES) {
                            String filePaths = projectItem.getSource();
                            List<File> files = new ArrayList<>();
                            for (String string : filePaths.split(",")) {
                                files.add(new File(string));
                            }
                            handleDragItem(files);
                        } else if (projectItem.type == ProjectItem.Type.IMAGE) {
                            handleDragItem(new Image(projectItem.getSource()));
                        } else {
                            handleDragItem(projectItem.source);
                        }
                    }
                } catch (JsonProcessingException ignored) {
                }
            }
        }

        public static final class ProjectItemData implements Serializable {
            private ProjectItem.Type type;
            private String source;

            public ProjectItemData() {
            }

            private ProjectItemData(ProjectItem.Type type, String source) {
                this.type = type;
                this.source = source;
            }

            public ProjectItem.Type getType() {
                return type;
            }

            public void setType(ProjectItem.Type type) {
                this.type = type;
            }

            public String getSource() {
                return source;
            }

            public void setSource(String source) {
                this.source = source;
            }
        }
    }

    private static final class CtrlListener {

        private boolean ctrlDown;

        public boolean isCtrlDown() {
            return ctrlDown;
        }

        public void setCtrlDown(boolean ctrlDown) {
            this.ctrlDown = ctrlDown;
        }

    }
}
