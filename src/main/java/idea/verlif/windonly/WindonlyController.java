package idea.verlif.windonly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import idea.verlif.windonly.components.MainContextMenu;
import idea.verlif.windonly.components.ProjectItem;
import idea.verlif.windonly.components.RemoteContextMenu;
import idea.verlif.windonly.components.RemoteProjectItem;
import idea.verlif.windonly.components.alert.ConfirmAlert;
import idea.verlif.windonly.components.alert.InputAlert;
import idea.verlif.windonly.components.item.FileItem;
import idea.verlif.windonly.components.item.ImageOne;
import idea.verlif.windonly.components.item.TextItem;
import idea.verlif.windonly.config.RemoteConfig;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.data.Archive;
import idea.verlif.windonly.data.Savable;
import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.remote.RemoteDataManager;
import idea.verlif.windonly.remote.RemoteItemData;
import idea.verlif.windonly.remote.RemoteListDisplay;
import idea.verlif.windonly.stage.EditPreviewer;
import idea.verlif.windonly.utils.ClipboardUtil;
import idea.verlif.windonly.utils.IpUtil;
import idea.verlif.windonly.utils.MessageUtil;
import idea.verlif.windonly.utils.SystemExecUtil;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
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
    public SplitPane splitPane;
    public ListView<RemoteProjectItem> remoteList;
    public ListView<ProjectItem> list;
    public ImageView lockView;
    public ImageView pinView;
    public ImageView slideView;
    public BorderPane center;
    public ChoiceBox<String> archiveBox;

    public Label ipView;

    private final RemoteDataManager remoteDataManager;
    private final ProjectItemManager projectItemManager;
    private Archive nowArchive;

    public WindonlyController() {
        remoteDataManager = RemoteDataManager.getInstance();
        projectItemManager = new ProjectItemManager();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        OnDragOver onDragOver = new OnDragOver();
        input.setOnDragOver(onDragOver);
        input.setOnDragDropped(new InputOnDrag());
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
        input.setOnKeyPressed(keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.V) {
                Object o = ClipboardUtil.getFormSystemClipboard();
                // 对CtrlV特殊处理
                Platform.runLater(() -> {
                    if (!(o instanceof String)) {
                        requestAddItem(o);
                    }
                });
            } else if (keyEvent.getCode() == KeyCode.ENTER && !input.getText().isEmpty()) {
                // 添加输入事项
                requestAddItem(input.getText());
                clearInput();
            }
        });
        // 设置list
        list.setFocusTraversable(false);
        // 设置数据展示区拖入事件
        list.setOnDragOver(onDragOver);
        list.setOnDragDropped(new ListOnDrag());
        // 右键菜单
        MainContextMenu<Event> contextMenu = new MainContextMenu<>() {
            @Override
            public ProjectItem onItem() {
                return getSelectedItem();
            }
        };
        list.setContextMenu(contextMenu);
        list.setOnContextMenuRequested(contextMenu);
        // 鼠标操作
        list.setOnMouseClicked(event -> {
            // ctrl左键直接复制
            if (event.getButton() == MouseButton.PRIMARY && event.isControlDown()) {
                new Message(Message.What.COPY).send();
            }
        });
        // 设置远端面板右键按钮
        remoteList.setContextMenu(new RemoteContextMenu());
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
        // 切换工作区
        archiveBox.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            load(newVal);
        });
        // 增加快捷方式
        archiveBox.setContextMenu(createArchiveMenu());
        // 注册监听
        registerHandler();
        refreshList();
        switchLock(WindonlyConfig.getInstance().isLock());
        switchPin(WindonlyConfig.getInstance().isAlwaysShow());
        switchSlide(WindonlyConfig.getInstance().isSlide());

        // 初始化远程列表
        initRemoteList();
        resize();
    }

    private void resize() {
        // 设置样式
        input.setFont(new Font(WindonlyConfig.getInstance().getButtonSize()));
        pinView.setFitHeight(WindonlyConfig.getInstance().getButtonSize());
        pinView.setFitWidth(WindonlyConfig.getInstance().getButtonSize());
        lockView.setFitHeight(pinView.getFitHeight());
        lockView.setFitWidth(pinView.getFitWidth());
        slideView.setFitHeight(pinView.getFitHeight());
        slideView.setFitWidth(pinView.getFitWidth());
        archiveBox.setPrefHeight(input.getPrefHeight());
        archiveBox.setPrefWidth(100);
    }

    private void refreshArchiveBox() {
        archiveBox.getItems().clear();
        archiveBox.getItems().addAll(Archive.allArchives());
    }

    private void selectArchive(String archive) {
        archiveBox.setValue(archive);
        Archive.setCurrentArchive(archive);
    }

    /**
     * 初始化远程列表
     */
    private void initRemoteList() {
        // 设置list
        remoteList.setFocusTraversable(false);
        // 设置数据展示区拖入事件
        remoteList.setOnDragEntered(new RemoteListOnDrag());
        remoteList.setOnDragEntered(new RemoteListOnDrag());
        remoteDataManager.init(remoteList);

        ipView.setFont(input.getFont());
        ipView.setText(IpUtil.getLocalIp());
        ipView.setContextMenu(remoteContextMenu());
    }

    private ContextMenu remoteContextMenu() {
        // 启用远程
        MenuItem enabled = new MenuItem(MessageUtil.get("enabled"));
        enabled.setDisable(!RemoteConfig.getInstance().isEnabled());
        // 关闭远程
        MenuItem disabled = new MenuItem(MessageUtil.get("disabled"));
        disabled.setDisable(RemoteConfig.getInstance().isEnabled());
        enabled.setOnAction(actionEvent -> {
            RemoteConfig.getInstance().setEnabled(true);
            enabled.setDisable(true);
            disabled.setDisable(false);
        });
        disabled.setOnAction(actionEvent -> {
            RemoteConfig.getInstance().setEnabled(false);
            enabled.setDisable(false);
            disabled.setDisable(true);
        });

        // 远程列表
        MenuItem remoteList = new MenuItem(MessageUtil.get("remoteList"));
        remoteList.setDisable(RemoteConfig.getInstance().isEnabled());
        remoteList.setOnAction(actionEvent -> {
            new RemoteListDisplay().show();
        });

        return new ContextMenu(enabled, disabled, remoteList);
    }

    private void initRemoteSocket() {

    }

    /**
     * 创建新的工作区
     */
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
            new InputAlert(MessageUtil.get("renameArchive"), Archive.getCurrentArchive()) {
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
                String archive = Archive.getCurrentArchive();
                new ConfirmAlert(MessageUtil.get("delArchive") + " - " + archive) {
                    @Override
                    public void confirm() {
                        Archive.delArchive(archive);
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

    private void refreshList() {
        String currentArchive = Archive.getCurrentArchive();
        refreshArchiveBox();
        // 切换到回当前分区
        selectArchive(currentArchive);
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

    /**
     * 编辑锁
     *
     * @param lock 是否锁定当前工作区内容
     */
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
     * 切换贴边
     *
     * @param slide 是否贴边
     */
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
        boolean slide = WindonlyConfig.getInstance().isSlide();
        nowArchive = new Archive(archive);
        nowArchive.load(WindonlyConfig.getInstance());
        nowArchive.load(projectItemManager);
        Archive.setCurrentArchive(archive);
        // 当从隐藏的工作区切换到不隐藏的工作区时，主动展开
        if (slide && !WindonlyConfig.getInstance().isSlide()) {
            new Message(Message.What.WINDOW_SLIDE_OUT).send();
        }
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

    private ProjectItem getSelectedItem() {
        ObservableList<ProjectItem> selectedItems = list.getSelectionModel().getSelectedItems();
        if (!selectedItems.isEmpty()) {
            return selectedItems.get(0);
        } else {
            return null;
        }
    }

    /**
     * 注册需要处理的信息
     */
    private void registerHandler() {
        new Handler() {
            @Override
            public void handlerMessage(Message message) {
                switch (message.what) {
                    case Message.What.DATA_REFRESH:
                        Platform.runLater(() -> {
                            refreshList();
                        });
                        break;
                    case Message.What.COPY: {
                        ProjectItem item = getSelectedItem();
                        if (item != null) {
                            ClipboardUtil.copyToSystemClipboard(item.getSource());
                        }
                    }
                    break;
                    case Message.What.DELETE: {
                        ProjectItem item = getSelectedItem();
                        Platform.runLater(() -> {
                            removeItem(item, true);
                            save();
                        });
                    }
                    break;
                    case Message.What.EDIT: {
                        ProjectItem item = getSelectedItem();
                        if (item != null) {
                            Platform.runLater(() -> {
                                new EditPreviewer(item).show();
                            });
                        }
                    }
                    break;
                    case Message.What.SET_TO_TOP: {
                        ProjectItem item = getSelectedItem();
                        Platform.runLater(() -> {
                            topItem(item, true);
                            save();
                        });
                    }
                    break;
                    case Message.What.OPEN_WITH_SYSTEM: {
                        ProjectItem item = getSelectedItem();
                        File file = getFileFromProjectItem(item);
                        if (file != null) {
                            SystemExecUtil.openFileByExplorer(file.getAbsolutePath());
                        }
                    }
                    break;
                    case Message.What.OPEN_WITH_EXPLORE: {
                        ProjectItem item = getSelectedItem();
                        File file = getFileFromProjectItem(item);
                        if (file != null) {
                            SystemExecUtil.selectFileByExplorer(file.getAbsolutePath());
                        }
                    }
                    break;
                    case Message.What.OPEN_WITH_BROWSE: {
                        ProjectItem item = getSelectedItem();
                        if (item != null) {
                            String url;
                            if (item.getType() == ProjectItem.Type.IMAGE) {
                                url = ((Image) item.getSource()).getUrl();
                            } else {
                                url = item.getSource().toString();
                            }
                            SystemExecUtil.openUrlByBrowser(url);
                        }
                    }
                    break;
                    case Message.What.WINDOW_PIN:
                        Platform.runLater(() -> switchPin(WindonlyConfig.getInstance().isAlwaysShow()));
                        break;
                    case Message.What.ARCHIVE_LOCK:
                        Platform.runLater(() -> switchLock(WindonlyConfig.getInstance().isLock()));
                        break;
                    case Message.What.ARCHIVE_SAVE:
                        Platform.runLater(() -> save());
                        break;
                    case Message.What.WINDOW_SLIDE:
                        Platform.runLater(() -> switchSlide(WindonlyConfig.getInstance().isSlide()));
                        break;
                    case Message.What.WINDOW_NOT_FOCUS:
                        if (WindonlyConfig.getInstance().isSlide()) {
                            new Message(Message.What.WINDOW_SLIDE_IN).send();
                        }
                        break;
                    case Message.What.WINDOW_CHANGED_WIDTH: {
                        double width = (double) message.getObj();
                        for (ProjectItem item : list.getItems()) {
                            item.setPrefWidth(width - 50);
                        }
                        for (ProjectItem item : remoteList.getItems()) {
                            item.setPrefWidth(width - 50);
                        }
                    }
                    break;
                }
            }
        };
    }

    private File getFileFromProjectItem(ProjectItem item) {
        if (item == null) {
            return null;
        }
        if (item.getType() == ProjectItem.Type.FILE) {
            return (File) item.getSource();
        } else {
            List<File> list = (List<File>) item.getSource();
            if (list.size() == 1) {
                return list.get(0);
            }
        }
        return null;
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
        } else if (o instanceof Image image) {
            ImageOne imageOne = new ImageOne(image);
            imageOne.init();
            projectItem = new ProjectItem(imageOne);
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
        if (projectItem != null) {
            projectItemManager.remove(projectItem, check);
            projectItemManager.add(0, projectItem, check);
        }
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
    private void requestAddItem(Object o) {
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
        projectItemManager.add(0, projectItem, check);
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
            new Message(Message.What.WINDOW_REQUIRE_HIDDEN).send();
        }
    }

    private static final class OnDragOver implements EventHandler<DragEvent> {
        @Override
        public void handle(DragEvent dragEvent) {
            dragEvent.acceptTransferModes(TransferMode.ANY);
            dragEvent.consume();
        }
    }

    /**
     * 输入框拖拽处理
     */
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

    /**
     * 列表拖拽处理
     */
    private class ListOnDrag implements EventHandler<DragEvent> {

        @Override
        public void handle(DragEvent dragEvent) {
            Dragboard dragboard = dragEvent.getDragboard();
            if (dragboard.hasFiles()) {
                handleDragItem(dragboard.getFiles());
            } else if (dragboard.hasImage()) {
                Image image = dragboard.getImage();
                handleDragItem(image);
            } else if (dragboard.hasUrl()) {
                handleDragItem(dragboard.getUrl());
            } else if (dragboard.hasString()) {
                handleDragItem(dragboard.getString());
            }
        }

        protected void handleDragItem(Object o) {
            WindonlyController.this.requestAddItem(o);
        }
    }

    /**
     * 远端列表拖拽处理
     */
    private final class RemoteListOnDrag extends ListOnDrag {

        /**
         * 处理拖拽进入的数据，并保存数据
         *
         * @param o 数据对象
         */
        protected void handleDragItem(Object o) {
            RemoteItemData remoteItemData = new RemoteItemData();
            RemoteProjectItem.Type type;
            String data;
            if (o instanceof List) {
                type = RemoteProjectItem.Type.FILES;
                List<File> source = (List<File>) o;
                StringBuilder s = new StringBuilder();
                for (File file : source) {
                    s.append(file.getAbsoluteFile()).append(",");
                }
                data = s.substring(0, s.length() - 1);
            } else if (o instanceof File) {
                type = RemoteProjectItem.Type.FILE;
                data = ((File) o).getAbsolutePath();
            } else if (o instanceof Image) {
                type = RemoteProjectItem.Type.IMAGE;
                data = ((Image) o).getUrl();
            } else {
                type = RemoteProjectItem.Type.TEXT;
                data = o.toString();
            }
            remoteItemData.setType(type);
            remoteItemData.setData(data);
            new Message(Message.What.INSERT_REMOTE).send(remoteItemData);
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
                            String source = projectItem.getSource();
                            if (source != null) {
                                addItem(new Image(projectItem.getSource()), false);
                            }
                        } else {
                            addItem(projectItem.getSource(), false);
                        }
                    }
                } catch (JsonProcessingException e) {
                    throw new WindonlyException(e);
                }
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
