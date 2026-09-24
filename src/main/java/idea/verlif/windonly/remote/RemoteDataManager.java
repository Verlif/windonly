package idea.verlif.windonly.remote;

import idea.verlif.windonly.components.RemoteProjectItem;
import idea.verlif.windonly.components.item.FileItem;
import idea.verlif.windonly.components.item.ImageOne;
import javafx.scene.image.Image;
import idea.verlif.windonly.components.item.TextItem;
import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.ClipboardUtil;
import idea.verlif.windonly.utils.IpUtil;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

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
                // 消息分发已经保证在 FX 线程执行，这里不再需要 runLater
                switch (message.what) {
                    case Message.What.COPY_REMOTE: {
                        RemoteProjectItem focusedItem = listView.getFocusModel().getFocusedItem();
                        if (focusedItem != null) {
                            ClipboardUtil.copyToSystemClipboard(focusedItem.getClipboardSource());
                        }
                    }
                    break;
                    case Message.What.DELETE_REMOTE: {
                        RemoteProjectItem focusedItem = listView.getFocusModel().getFocusedItem();
                        if (focusedItem != null) {
                            listView.getItems().remove(focusedItem);
                        }
                    }
                    break;
                    case Message.What.INSERT_REMOTE: {
                        RemoteItemData remoteItemData = (RemoteItemData) message.getObj();
                        if (remoteItemData == null) {
                            break;
                        }
                        switch (remoteItemData.getType()) {
                            case TEXT:
                                addItem(remoteItemData.getData());
                                break;
                            case IMAGE:
                                addItem(ImageOne.ofUrl(remoteItemData.getData()));
                                break;
                            case FILE:
                                addItem(new File(remoteItemData.getData()));
                                break;
                            case FILES: {
                                String filePaths = remoteItemData.getData();
                                List<File> files = new ArrayList<>();
                                for (String string : filePaths.split(",")) {
                                    files.add(new File(string));
                                }
                                addItem(files);
                                break;
                            }
                            default:
                                break;
                        }
                    }
                    break;
                    default:
                        break;
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
        addItem(o);
    }

    /**
     * 向数据添加
     */
    private boolean addItem(Object o) {
        if (o == null) {
            return false;
        }
        // 已经构建好的图片项（按显示尺寸解码过的）
        if (o instanceof ImageOne imageOne) {
            List<RemoteProjectItem> items = listView.getItems();
            if (!items.isEmpty() && items.stream().anyMatch(item -> item.sourceEquals(imageOne.getSource()))) {
                return false;
            }
            imageOne.init();
            return items.add(new RemoteProjectItem(imageOne, IpUtil.getLocalIp()));
        }
        List<RemoteProjectItem> all = listView.getItems();
        if (!all.isEmpty() && all.stream().anyMatch(projectItem -> projectItem.sourceEquals(o))) {
            return false;
        }
        RemoteProjectItem projectItem = selectProjectItem(o);
        if (projectItem != null) {
            return listView.getItems().add(projectItem);
        }
        return false;
    }

    private RemoteProjectItem selectProjectItem(Object o) {
        RemoteProjectItem projectItem;
        if (o instanceof List files) {
            FileItem fileItem = new FileItem(files);
            fileItem.init();
            projectItem = new RemoteProjectItem(fileItem, IpUtil.getLocalIp());
        } else if (o instanceof File) {
            FileItem fileItem = new FileItem((File) o);
            fileItem.init();
            projectItem = new RemoteProjectItem(fileItem, IpUtil.getLocalIp());
        } else if (o instanceof Image image) {
            if (image.getUrl() == null) {
                return null;
            }
            ImageOne imageOne = new ImageOne(image);
            imageOne.init();
            projectItem = new RemoteProjectItem(imageOne, IpUtil.getLocalIp());
        } else {
            TextItem textItem = new TextItem(o.toString());
            textItem.init();
            projectItem = new RemoteProjectItem(textItem, IpUtil.getLocalIp());
        }
        return projectItem;
    }
}
