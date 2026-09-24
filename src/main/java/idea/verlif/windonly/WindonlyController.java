package idea.verlif.windonly;

import com.fasterxml.jackson.databind.JavaType;
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
import idea.verlif.windonly.utils.JsonUtil;
import idea.verlif.windonly.utils.MessageUtil;
import idea.verlif.windonly.utils.SystemExecUtil;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.util.Duration;

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

    /**
     * 正在刷新工作区下拉框，期间的 value 变化不触发加载
     */
    private boolean archiveBoxUpdating;
    /**
     * 存档是否有未落盘的改动
     */
    private boolean savePending;
    /**
     * 存档合并写入。短时间内连续增删数据时不再每次都写整个文件。
     */
    private final PauseTransition saveDelay = new PauseTransition(Duration.millis(400));

    /**
     * 最近一次设置的列表宽度，宽度没变时不再遍历所有数据项
     */
    private double lastItemWidth = -1;
    /**
     * 图标当前状态，避免重复读取资源图片
     */
    private Boolean pinState;
    private Boolean lockState;
    private Boolean slideState;

    public WindonlyController() {
        remoteDataManager = RemoteDataManager.getInstance();
        projectItemManager = new ProjectItemManager();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saveDelay.setOnFinished(event -> saveNow(false));
        OnDragOver onDragOver = new OnDragOver();
        input.setOnDragOver(onDragOver);
        input.setOnDragDropped(new InputOnDrag());
        // 添加搜索监听
        input.textProperty().addListener((observableValue, oldVal, newVal) -> projectItemManager.applyFilter(newVal));
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
            // 刷新下拉框时 clear() 会把 value 置为 null，原实现会因此多加载一次 null 工作区
            if (archiveBoxUpdating || newVal == null || newVal.equals(oldVal)) {
                return;
            }
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
        archiveBoxUpdating = true;
        try {
            archiveBox.getItems().setAll(Archive.allArchives());
        } finally {
            archiveBoxUpdating = false;
        }
    }

    private void selectArchive(String archive) {
        if (archive == null) {
            return;
        }
        // 静默设置下拉框选中项，加载由 load() 统一负责，避免监听器再触发一次
        archiveBoxUpdating = true;
        try {
            archiveBox.setValue(archive);
        } finally {
            archiveBoxUpdating = false;
        }
        load(archive);
    }

    /**
     * 初始化远程列表
     */
    private void initRemoteList() {
        // 设置list
        remoteList.setFocusTraversable(false);
        // 设置数据展示区拖入事件。
        // 原实现连续两次调用 setOnDragEntered（后者覆盖前者，等于没有处理释放事件），
        // 导致只是"拖过"远程列表就会把数据发出去。这里与本地列表保持一致，只在释放时处理。
        remoteList.setOnDragOver(new OnDragOver());
        remoteList.setOnDragDropped(new RemoteListOnDrag());
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
        // 工作区可能已经被删除，回退到列表中的第一个
        if (currentArchive == null || !archiveBox.getItems().contains(currentArchive)) {
            if (archiveBox.getItems().isEmpty()) {
                return;
            }
            currentArchive = archiveBox.getItems().get(0);
        }
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
        if (pinState != null && pinState == pin) {
            return;
        }
        pinState = pin;
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
        if (lockState != null && lockState == lock) {
            return;
        }
        lockState = lock;
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
        if (slideState != null && slideState == slide) {
            return;
        }
        slideState = slide;
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
        if (archive == null) {
            return;
        }
        // 切换前先把上一个工作区未落盘的改动保存掉
        flushSave();
        boolean slide = WindonlyConfig.getInstance().isSlide();
        // 先清掉旧工作区的数据与过滤条件，避免带着旧条件装载新工作区
        projectItemManager.clear(false);
        projectItemManager.resetFilter();
        input.setText("");
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
     * 标记存档有改动，合并到一次写入。
     * <p>
     * 原实现每次增删改都会把整个工作区序列化并写文件，
     * 连续操作（例如连续拖入多个文件）就会产生大量重复的序列化与磁盘写入。
     */
    private void scheduleSave() {
        savePending = true;
        saveDelay.playFromStart();
    }

    /**
     * 立刻把未落盘的改动写入存档
     */
    private void flushSave() {
        saveNow(false);
    }

    /**
     * 强制执行一次存档（窗口关闭、编辑保存等场景）
     */
    private void saveNow(boolean force) {
        if (saveDelay.getStatus() == Animation.Status.RUNNING) {
            saveDelay.stop();
        }
        if (!savePending && !force) {
            return;
        }
        savePending = false;
        if (nowArchive != null) {
            try {
                nowArchive.save(projectItemManager);
            } catch (Throwable e) {
                // 存档失败不应该打断界面操作
                System.err.println("Cannot save archive - " + e.getMessage());
            }
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
     * <p>
     * 消息分发已经保证在 FX 线程执行，因此这里不再需要 Platform.runLater 包装。
     */
    private void registerHandler() {
        new Handler() {
            @Override
            public void handlerMessage(Message message) {
                switch (message.what) {
                    case Message.What.DATA_REFRESH:
                        refreshList();
                        break;
                    case Message.What.COPY: {
                        ProjectItem item = getSelectedItem();
                        if (item != null) {
                            ClipboardUtil.copyToSystemClipboard(item.getClipboardSource());
                        }
                    }
                    break;
                    case Message.What.DELETE: {
                        ProjectItem item = getSelectedItem();
                        removeItem(item, true);
                        scheduleSave();
                    }
                    break;
                    case Message.What.EDIT: {
                        ProjectItem item = getSelectedItem();
                        if (item != null) {
                            new EditPreviewer(item).show();
                        }
                    }
                    break;
                    case Message.What.SET_TO_TOP: {
                        ProjectItem item = getSelectedItem();
                        topItem(item, true);
                        scheduleSave();
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
                        switchPin(WindonlyConfig.getInstance().isAlwaysShow());
                        break;
                    case Message.What.ARCHIVE_LOCK:
                        switchLock(WindonlyConfig.getInstance().isLock());
                        break;
                    case Message.What.ARCHIVE_SAVE:
                        // 窗口关闭、编辑保存等场景必须立刻落盘
                        saveNow(true);
                        break;
                    case Message.What.WINDOW_SLIDE:
                        switchSlide(WindonlyConfig.getInstance().isSlide());
                        break;
                    case Message.What.WINDOW_NOT_FOCUS:
                        if (WindonlyConfig.getInstance().isSlide()) {
                            new Message(Message.What.WINDOW_SLIDE_IN).send();
                        }
                        break;
                    case Message.What.WINDOW_CHANGED_WIDTH: {
                        double width = (double) message.getObj();
                        if (width == lastItemWidth) {
                            break;
                        }
                        lastItemWidth = width;
                        double prefWidth = width - 50;
                        // 用完整数据列表，保证被搜索过滤掉的数据项宽度也是最新的
                        for (ProjectItem item : projectItemManager.getAll()) {
                            item.setPrefWidth(prefWidth);
                        }
                        for (ProjectItem item : remoteList.getItems()) {
                            item.setPrefWidth(prefWidth);
                        }
                    }
                    break;
                    default:
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
        }
        if (item.getType() == ProjectItem.Type.FILES) {
            List<File> files = (List<File>) item.getSource();
            if (files.size() == 1) {
                return files.get(0);
            }
        }
        return null;
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
        if (o == null) {
            return;
        }
        // 去除重复添加。原实现用 Stream 一次性遍历，这里改为普通循环，避免额外的对象创建。
        List<ProjectItem> all = projectItemManager.getAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).sourceEquals(o)) {
                return;
            }
        }
        addItem(o, true);
        scheduleSave();
    }

    /**
     * 向数据添加
     */
    private void addItem(Object o, boolean check) {
        Node node = createItemNode(o);
        if (node == null) {
            return;
        }
        projectItemManager.add(0, new ProjectItem(node), check);
    }

    /**
     * 根据数据对象创建展示节点
     */
    private Node createItemNode(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof ImageOne imageOne) {
            imageOne.init();
            return imageOne;
        } else if (o instanceof List) {
            FileItem fileItem = new FileItem((List<File>) o);
            fileItem.init();
            return fileItem;
        } else if (o instanceof File) {
            FileItem fileItem = new FileItem((File) o);
            fileItem.init();
            return fileItem;
        } else if (o instanceof Image image) {
            ImageOne imageOne = new ImageOne(image);
            imageOne.init();
            return imageOne;
        } else {
            TextItem textItem = new TextItem(o.toString());
            textItem.init();
            return textItem;
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
     * <p>
     * {@code items} 是完整的数据（模型），{@code list.getItems()} 是当前展示的内容（视图）。
     * 搜索过滤时只重建视图，数据本身不受影响。
     */
    private final class ProjectItemManager implements Savable<String> {

        private final List<ProjectItem> items = new ArrayList<>();
        /**
         * 当前搜索关键字，空串表示不过滤
         */
        private String filter = "";

        private ProjectItemManager() {
        }

        public void add(ProjectItem projectItem, boolean check) {
            if (!check || checkAccess()) {
                items.add(projectItem);
                if (filter.isEmpty()) {
                    list.getItems().add(projectItem);
                } else {
                    syncView();
                }
            }
        }

        public void add(int index, ProjectItem projectItem, boolean check) {
            if (!check || checkAccess()) {
                items.add(index, projectItem);
                if (filter.isEmpty()) {
                    list.getItems().add(index, projectItem);
                } else {
                    syncView();
                }
            }
        }

        public void remove(ProjectItem projectItem, boolean check) {
            if (!check || checkAccess()) {
                items.remove(projectItem);
                if (filter.isEmpty()) {
                    list.getItems().remove(projectItem);
                } else {
                    syncView();
                }
            }
        }

        public List<ProjectItem> getAll() {
            return items;
        }

        public void clear(boolean check) {
            if (!check || checkAccess()) {
                items.clear();
                list.getItems().clear();
            }
        }

        /**
         * 应用搜索关键字
         */
        public void applyFilter(String key) {
            String value = key == null ? "" : key;
            if (filter.equals(value)) {
                return;
            }
            this.filter = value;
            syncView();
        }

        /**
         * 直接清空过滤条件（不重建视图），用于工作区切换
         */
        public void resetFilter() {
            this.filter = "";
        }

        private void syncView() {
            if (filter.isEmpty()) {
                list.getItems().setAll(items);
                return;
            }
            List<ProjectItem> search = new ArrayList<>(items.size());
            for (ProjectItem item : items) {
                if (item.match(filter)) {
                    search.add(item);
                }
            }
            // 一次性替换，避免 clear + addAll 触发两次列表变更
            list.getItems().setAll(search);
        }

        @Override
        public String save() {
            List<ProjectItemData> dataList = new ArrayList<>(items.size());
            for (ProjectItem projectItem : items) {
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
                dataList.add(data);
            }
            return JsonUtil.writePretty(dataList);
        }

        @Override
        public void load(String s) {
            clear(false);
            if (s != null && !s.isEmpty()) {
                try {
                    JavaType javaType = JsonUtil.mapper().getTypeFactory().constructParametricType(List.class, ProjectItemData.class);
                    List<ProjectItemData> dataList = JsonUtil.mapper().readValue(s, javaType);
                    if (dataList == null) {
                        return;
                    }
                    // 反向装载
                    for (int i = dataList.size() - 1; i > -1; i--) {
                        ProjectItemData projectItem = dataList.get(i);
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
                                // 图片按列表显示尺寸解码，避免为一张缩略图保留整张原图的像素
                                addItem(ImageOne.ofUrl(source), false);
                            }
                        } else {
                            addItem(projectItem.getSource(), false);
                        }
                    }
                } catch (Exception e) {
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
