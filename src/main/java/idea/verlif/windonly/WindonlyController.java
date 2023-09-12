package idea.verlif.windonly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import idea.verlif.windonly.components.ProjectItem;
import idea.verlif.windonly.components.RemoteProjectItem;
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
import idea.verlif.windonly.utils.IpUtil;
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
    public ListView<RemoteProjectItem> remoteList;
    public ListView<ProjectItem> list;
    public ImageView lockView;
    public ImageView pinView;
    public ImageView slideView;
    public BorderPane center;
    public ChoiceBox<String> archiveBox;

    public Label ipView;

    private final ProjectItemManager projectItemManager;
    private Archive nowArchive;

    public WindonlyController() {
        projectItemManager = new ProjectItemManager();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        pinView.getParent().setOnMouseClicked(mouseEvent -> {
            boolean alwaysShow = WindonlyConfig.getInstance().isAlwaysShow();
            WindonlyConfig.getInstance().setAlwaysShow(!alwaysShow);
        });
        // 设置锁
        lockView.getParent().setOnMouseClicked(mouseEvent -> {
            boolean lock = WindonlyConfig.getInstance().isLock();
            WindonlyConfig.getInstance().setLock(!lock);
        });
        // 设置贴边收起
        slideView.getParent().setOnMouseClicked(mouseEvent -> {
            boolean slide = WindonlyConfig.getInstance().isSlide();
            WindonlyConfig.getInstance().setSlide(!slide);
        });
        // 工作区设定
        archiveBox.setPadding(new Insets(4, 0, 5, 0));
        archiveBox.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            // 保存当前的数据
            save();
            load(newVal);
        });
        // 增加快捷方式
        archiveBox.setContextMenu(createArchiveMenu());
        // 初始化工作区存档
        refreshArchiveBox();
        // 注册监听
        registerHandler();
        // 切换到回当前分区
        selectArchive(Archive.getCurrentArchive());
        switchPin(WindonlyConfig.getInstance().isAlwaysShow());
        switchSlide(WindonlyConfig.getInstance().isSlide());

        // 初始化远程列表
        initRemoteList();
        // 设置样式
        input.setPrefHeight(WindonlyConfig.getInstance().getFontSize() + 8);
        input.setFont(new Font(WindonlyConfig.getInstance().getFontSize()));
        pinView.setFitHeight(WindonlyConfig.getInstance().getFontSize());
        pinView.setFitWidth(WindonlyConfig.getInstance().getFontSize());
        lockView.setFitHeight(pinView.getFitHeight());
        lockView.setFitWidth(pinView.getFitWidth());
        slideView.setFitHeight(pinView.getFitHeight());
        slideView.setFitWidth(pinView.getFitWidth());
        archiveBox.setPrefHeight(input.getPrefHeight());
        archiveBox.setPrefWidth(100 * WindonlyConfig.getInstance().getMagnification());
    }

    private void refreshArchiveBox() {
        archiveBox.getItems().clear();
        archiveBox.getItems().addAll(Archive.allArchives());
    }

    private void selectArchive(String archive) {
        archiveBox.setValue(archive);
    }

    /**
     * 初始化远程列表
     */
    private void initRemoteList() {
        // 设置list
        remoteList.setFocusTraversable(false);
        // 设置数据展示区拖入事件
        remoteList.setOnDragEntered(new ListOnDrag());
        remoteList.setOnDragEntered(new RemoteListOnDrag());

        ipView.setFont(input.getFont());
        ipView.setText(IpUtil.getLocalIp());

        // 注册远程信息处理
        registerRemoteHandler();
    }

    private void initRemoteSocket() {

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
            if (checkAccess()) {
                new ConfirmAlert(MessageUtil.get("delArchive") + " - " + Archive.getCurrentArchive()) {
                    @Override
                    public void confirm() {
                        Archive.delArchive(Archive.getCurrentArchive());
                        refreshArchiveBox();
                        // 切换到回当前分区
                        selectArchive(Archive.allArchives().get(0));
                    }
                }.show();
            }
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

    private void switchSlide(boolean slide) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(slide ? "/images/slide.png" : "/images/unslide.png")) {
            if (resourceAsStream != null) {
                slideView.setImage(new Image(resourceAsStream));
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
        if (nowArchive != null) {
            nowArchive.save(projectItemManager);
        }
    }

    private void clearInput() {
        input.setText("");
    }

    /**
     * 注册需要处理的信息
     */
    private void registerHandler() {
        new Handler() {
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
                            removeItem(focusedItem, true);
                            save();
                        });
                    }
                    case Message.What.SET_TO_TOP -> {
                        ProjectItem focusedItem = list.getFocusModel().getFocusedItem();
                        Platform.runLater(() -> {
                            topItem(focusedItem, true);
                            save();
                        });
                    }
                    case Message.What.WINDOW_PIN ->
                            Platform.runLater(() -> switchPin(WindonlyConfig.getInstance().isAlwaysShow()));
                    case Message.What.ARCHIVE_LOCK ->
                            Platform.runLater(() -> switchLock(WindonlyConfig.getInstance().isLock()));
                    case Message.What.ARCHIVE_SAVE -> Platform.runLater(() -> save());
                    case Message.What.WINDOW_SLIDE ->
                            Platform.runLater(() -> switchSlide(WindonlyConfig.getInstance().isSlide()));
                }
            }
        };
    }

    private void registerRemoteHandler() {
        new Handler("remote") {
            @Override
            public void handlerMessage(Message message) {
                switch (message.what) {
                    case Message.What.COPY_REMOTE -> {
                        RemoteProjectItem focusedItem = remoteList.getFocusModel().getFocusedItem();
                        ClipboardUtil.copyToSystemClipboard(focusedItem.getSource());
                    }
                    case Message.What.DELETE_REMOTE -> {
                        RemoteProjectItem focusedItem = remoteList.getFocusModel().getFocusedItem();
                        Platform.runLater(() -> {
                            remoteList.getItems().remove(focusedItem);
                        });
                    }
                    case Message.What.SET_TO_TOP_REMOTE -> {
                        RemoteProjectItem focusedItem = remoteList.getFocusModel().getFocusedItem();
                        Platform.runLater(() -> {
                            remoteList.getItems().remove(focusedItem);
                            remoteList.getItems().add(0, focusedItem);
                        });
                    }
                }
            }
        };
    }

    private ProjectItem selectProjectItem(Object o) {
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
                return null;
            }
        } else {
            TextItem textItem = new TextItem(o.toString());
            textItem.init();
            projectItem = new ProjectItem(textItem);
        }
        return projectItem;
    }

    /**
     * 从数据删除
     *
     * @param projectItem 删除的项目
     */
    private void removeItem(ProjectItem projectItem, boolean check) {
        projectItemManager.remove(projectItem, check);
    }

    /**
     * 置顶数据
     *
     * @param projectItem 目标置顶项目
     */
    private void topItem(ProjectItem projectItem, boolean check) {
        projectItemManager.remove(projectItem, check);
        projectItemManager.add(0, projectItem, check);
    }

    /**
     * 检测操作是否可用
     */
    private boolean checkAccess() {
        if (WindonlyConfig.getInstance().isLock()) {
            showTip(MessageUtil.get("notModifyArchiveWhenLocked"));
            return false;
        } else {
            return true;
        }
    }

    /**
     * 处理拖拽进入的数据，并保存数据
     *
     * @param o 数据对象
     */
    private void handleDragItem(Object o) {
        // 去除重复添加
        List<ProjectItem> all = projectItemManager.getAll();
        if (!all.isEmpty() && all.stream().anyMatch(projectItem -> projectItem.sourceEquals(o))) {
            return;
        }
        addItem(o, true);
        save();
    }

    /**
     * 向数据添加
     */
    private void addItem(Object o, boolean check) {
        ProjectItem projectItem = selectProjectItem(o);
        if (projectItem != null) {
            projectItemManager.add(0, projectItem, check);
        }
    }

    @FXML
    protected void inputClicked() {
    }

    public void onMouseEntered() {
        if (WindonlyConfig.getInstance().isSlide()) {
            new Message(Message.What.WINDOW_SLIDE_OUT).send();
        }
    }

    public void onMouseExited() {
        if (WindonlyConfig.getInstance().isSlide()) {
            new Message(Message.What.WINDOW_SLIDE_IN).send();
        }
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

    private final class RemoteListOnDrag implements EventHandler<DragEvent> {

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

        /**
         * 处理拖拽进入的数据，并保存数据
         *
         * @param o 数据对象
         */
        private void handleDragItem(Object o) {
            // 去除重复添加
            List<RemoteProjectItem> all = remoteList.getItems();
            if (!all.isEmpty() && all.stream().anyMatch(projectItem -> projectItem.sourceEquals(o))) {
                return;
            }
            addItem(o);
        }

        /**
         * 向数据添加
         */
        private void addItem(Object o) {
            RemoteProjectItem projectItem = selectProjectItem(o);
            if (projectItem != null) {
                remoteList.getItems().add(projectItem);
            }
        }

        private RemoteProjectItem selectProjectItem(Object o) {
            RemoteProjectItem projectItem;
            if (o instanceof List) {
                FileItem fileItem = new FileItem((List<File>) o);
                fileItem.init();
                projectItem = new RemoteProjectItem(fileItem, IpUtil.getLocalIp());
            } else if (o instanceof File) {
                FileItem fileItem = new FileItem((File) o);
                fileItem.init();
                projectItem = new RemoteProjectItem(fileItem, IpUtil.getLocalIp());
            } else if (o instanceof Image) {
                if (((Image) o).getUrl() != null) {
                    ImageOne imageOne = new ImageOne((Image) o);
                    imageOne.init();
                    projectItem = new RemoteProjectItem(imageOne, IpUtil.getLocalIp());
                } else {
                    return null;
                }
            } else {
                TextItem textItem = new TextItem(o.toString());
                textItem.init();
                projectItem = new RemoteProjectItem(textItem, IpUtil.getLocalIp());
            }
            return projectItem;
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

        public void add(ProjectItem projectItem, boolean check) {
            if (!check || checkAccess()) {
                list.getItems().add(projectItem);
                this.all.add(projectItem);
            }
        }

        public void add(int index, ProjectItem projectItem, boolean check) {
            if (!check || checkAccess()) {
                list.getItems().add(index, projectItem);
                this.all.add(index, projectItem);
            }
        }

        public void remove(ProjectItem projectItem, boolean check) {
            if (!check || checkAccess()) {
                list.getItems().remove(projectItem);
                this.all.remove(projectItem);
            }
        }

        public void resetProjectItems() {
            list.getItems().clear();
            list.getItems().addAll(this.all);
        }

        public List<ProjectItem> getAll() {
            return all;
        }

        public void clear(boolean check) {
            if (!check || checkAccess()) {
                list.getItems().clear();
                this.all.clear();
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
            clear(false);
            if (s != null && !s.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, ProjectItemData.class);
                    List<ProjectItemData> list = mapper.readValue(s, javaType);
                    // 反向装载
                    for (int i = list.size() - 1; i > -1; i--) {
                        ProjectItemData projectItem = list.get(i);
                        if (projectItem.getType() == ProjectItem.Type.FILE) {
                            addItem(new File(projectItem.getSource()), false);
                        } else if (projectItem.getType() == ProjectItem.Type.FILES) {
                            String filePaths = projectItem.getSource();
                            List<File> files = new ArrayList<>();
                            for (String string : filePaths.split(",")) {
                                files.add(new File(string));
                            }
                            addItem(files, false);
                        } else if (projectItem.getType() == ProjectItem.Type.IMAGE) {
                            addItem(new Image(projectItem.getSource()), false);
                        } else {
                            addItem(projectItem.getSource(), false);
                        }
                    }
                } catch (JsonProcessingException e) {
                    throw new WindonlyException(e);
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

}
