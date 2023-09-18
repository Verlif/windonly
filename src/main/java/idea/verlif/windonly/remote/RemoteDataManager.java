package idea.verlif.windonly.remote;

import idea.verlif.windonly.components.RemoteProjectItem;
import idea.verlif.windonly.components.item.FileItem;
import idea.verlif.windonly.components.item.ImageOne;
import idea.verlif.windonly.components.item.TextItem;
import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.ClipboardUtil;
import idea.verlif.windonly.utils.IpUtil;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RemoteDataManager {

    private static final RemoteDataManager REMOTE_DATA_MANAGER = new RemoteDataManager();

    private ListView<RemoteProjectItem> listView;

    private RemoteDataManager() {
    }

    public static RemoteDataManager getInstance() {
        return REMOTE_DATA_MANAGER;
    }

    public void init(ListView<RemoteProjectItem> listView) {
        this.listView = listView;

        registerRemoteHandler();
    }

    private void registerRemoteHandler() {
        new Handler() {
            @Override
            public void handlerMessage(Message message) {
                switch (message.what) {
                    case Message.What.COPY_REMOTE -> {
                        RemoteProjectItem focusedItem = listView.getFocusModel().getFocusedItem();
                        ClipboardUtil.copyToSystemClipboard(focusedItem.getSource());
                    }
                    case Message.What.DELETE_REMOTE -> {
                        RemoteProjectItem focusedItem = listView.getFocusModel().getFocusedItem();
                        Platform.runLater(() -> {
                            listView.getItems().remove(focusedItem);
                        });
                    }
                    case Message.What.INSERT_REMOTE -> {
                        RemoteItemData remoteItemData = (RemoteItemData) message.getObj();
                        Platform.runLater(() -> {
                            switch (remoteItemData.getType()) {
                                case TEXT -> addItem(remoteItemData.getData());
                                case IMAGE -> addItem(new Image(remoteItemData.getData()));
                                case FILE -> addItem(new File(remoteItemData.getData()));
                                case FILES -> {
                                    String filePaths = remoteItemData.getData();
                                    List<File> files = new ArrayList<>();
                                    for (String string : filePaths.split(",")) {
                                        files.add(new File(string));
                                    }
                                    addItem(files);
                                }
                            }
                        });
                    }
                }
            }
        };
    }

    public synchronized void remove(RemoteItemData remoteItemData) {
        int target = -1;
        ObservableList<RemoteProjectItem> items = listView.getItems();
        for (int i = 0; i < items.size(); i++) {
            RemoteProjectItem item = items.get(i);
            if (item.getKey().equals(remoteItemData.getKey())) {
                target = i;
                break;
            }
        }
        if (target > -1) {
            items.remove(target);
        }
    }

    public synchronized void add(RemoteItemData remoteItemData) {
        // 去除重复添加
        Object o = remoteItemData.getData();
        List<RemoteProjectItem> all = listView.getItems();
        if (!all.isEmpty() && all.stream().anyMatch(projectItem -> projectItem.sourceEquals(o))) {
            return;
        }
        addItem(o);
    }

    /**
     * 向数据添加
     */
    private boolean addItem(Object o) {
        RemoteProjectItem projectItem = selectProjectItem(o);
        if (projectItem != null) {
            return listView.getItems().add(projectItem);
        }
        return false;
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
