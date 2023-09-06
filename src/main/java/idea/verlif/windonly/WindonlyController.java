package idea.verlif.windonly;

import idea.verlif.windonly.components.item.FileItem;
import idea.verlif.windonly.components.ProjectItem;
import idea.verlif.windonly.components.item.TextItem;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.manage.HandlerManager;
import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.ClipboardUtil;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
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
    public List<ProjectItem> all;
    public ImageView pinView;

    public WindonlyController() {
        all = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        input.setPrefHeight(WindonlyConfig.getInstance().getFontSize() + 8);
        input.setFont(new Font(WindonlyConfig.getInstance().getFontSize()));
        input.setOnDragEntered(new InputOnDrag());
        // 搜索
        input.textProperty().addListener((observableValue, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                // 为空显示所有
                list.getItems().clear();
                list.getItems().addAll(all);
            } else {
                // 有输入值时，显示过滤列表
                list.getItems().clear();
                List<ProjectItem> search = new ArrayList<>(all.size());
                for (ProjectItem item : all) {
                    if (item.match(newVal)) {
                        search.add(item);
                    }
                }
                list.getItems().addAll(search);
            }
        });
        // 添加输入
        input.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER && !input.getText().isEmpty()) {
                addToList(input.getText());
            }
        });
        // 设置list
        list.setFocusTraversable(false);
        list.setOnDragEntered(new ListOnDrag());
        // 设置pin
        pinView.setFitHeight(WindonlyConfig.getInstance().getFontSize());
        pinView.setFitWidth(WindonlyConfig.getInstance().getFontSize());
        switchPin(WindonlyConfig.getInstance().isAlwaysShow());
        pinView.getParent().setOnMouseClicked(mouseEvent -> {
            WindonlyConfig.getInstance().setAlwaysShow(!WindonlyConfig.getInstance().isAlwaysShow());
            switchPin(WindonlyConfig.getInstance().isAlwaysShow());
        });

        registerHandler();
    }

    private void switchPin(boolean pin) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(pin ? "/images/pin.png" : "/images/unpin.png")) {
            if (resourceAsStream != null) {
                pinView.setImage(new Image(resourceAsStream));
                WindonlyApplication.getMainStage().setAlwaysOnTop(pin);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void inputClicked() {
    }

    private void clearList() {
        list.getItems().clear();
        all.clear();
    }

    private void clearInput() {
        input.setText("");
    }

    private void clear() {
        clearInput();
        clearList();
    }

    private void registerHandler() {
        HandlerManager.getInstance().addHandler(new Handler() {
            @Override
            public void handlerMessage(Message message) {
                if (message.what == Message.What.COPY) {
                    ProjectItem focusedItem = list.getFocusModel().getFocusedItem();
                    ClipboardUtil.copyToSystemClipboard(focusedItem.getTarget());
                } else if (message.what == Message.What.SET_TO_TOP) {
                    ProjectItem focusedItem = list.getFocusModel().getFocusedItem();
                    Platform.runLater(() -> {
                        list.getItems().remove(focusedItem);
                        all.remove(focusedItem);
                        list.getItems().add(0, focusedItem);
                        all.add(0, focusedItem);
                    });
                } else if (message.what == Message.What.DELETE) {
                    ProjectItem focusedItem = list.getFocusModel().getFocusedItem();
                    Platform.runLater(() -> {
                        list.getItems().remove(focusedItem);
                        all.remove(focusedItem);
                    });
                }
            }
        });
    }

    private void handleDragItem(Object o) {
        // 去除重复添加
        if (!all.isEmpty() && all.get(0).sourceEquals(o)) {
            return;
        }
        if (o instanceof List) {
            addToList((List<File>) o);
        } else if (o instanceof File) {
            addToList((File) o);
        } else if (o instanceof Image) {
            addToList("image - " + ((Image) o).getUrl());
        } else {
            String s = o.toString();
            // TODO: Base64处理
            addToList(s);
        }
    }

    private void addToList(String text) {
        TextItem textItem = new TextItem(text);
        textItem.init();
        ProjectItem projectItem = new ProjectItem(textItem);
        all.add(0, projectItem);
        list.getItems().add(0, projectItem);
    }

    private void addToList(List<File> files) {
        FileItem fileItem = new FileItem(files);
        fileItem.init();
        ProjectItem projectItem = new ProjectItem(fileItem);
        all.add(0, projectItem);
        list.getItems().add(0, projectItem);
    }

    private void addToList(File file) {
        FileItem fileItem = new FileItem(file);
        fileItem.init();
        ProjectItem projectItem = new ProjectItem(fileItem);
        all.add(0, projectItem);
        list.getItems().add(0, projectItem);
    }

    private final class InputOnDrag implements EventHandler<DragEvent> {

        @Override
        public void handle(DragEvent dragEvent) {
            Dragboard dragboard = dragEvent.getDragboard();
            if (dragboard.hasFiles()) {
                List<File> files = dragboard.getFiles();
                input.setText(files.get(0).getName());
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
}
