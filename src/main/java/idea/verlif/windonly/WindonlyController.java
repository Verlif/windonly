package idea.verlif.windonly;

import idea.verlif.windonly.components.item.FileItem;
import idea.verlif.windonly.components.ProjectItem;
import idea.verlif.windonly.components.item.ImageOne;
import idea.verlif.windonly.components.item.TextItem;
import idea.verlif.windonly.config.WindonlyConfig;
import idea.verlif.windonly.manage.HandlerManager;
import idea.verlif.windonly.manage.inner.Handler;
import idea.verlif.windonly.manage.inner.Message;
import idea.verlif.windonly.utils.ClipboardUtil;
import javafx.application.Platform;
import javafx.event.EventHandler;
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
    public ImageView pinView;

    private final ProjectItemManager projectItemManager;

    public WindonlyController() {
        projectItemManager = new ProjectItemManager();
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
        // 添加输入
        input.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER && !input.getText().isEmpty()) {
                addToList(input.getText());
                clearInput();
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
        projectItemManager.clear();
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
                switch (message.what) {
                    case Message.What.COPY -> {
                        ProjectItem focusedItem = list.getFocusModel().getFocusedItem();
                        ClipboardUtil.copyToSystemClipboard(focusedItem.getTarget());
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
                }
            }
        });
    }

    private void handleDragItem(Object o) {
        // 去除重复添加
        List<ProjectItem> all = projectItemManager.getAll();
        if (!all.isEmpty() && all.stream().anyMatch(projectItem -> projectItem.sourceEquals(o))) {
            return;
        }
        if (o instanceof List) {
            addToList((List<File>) o);
        } else if (o instanceof File) {
            addToList((File) o);
        } else if (o instanceof Image) {
            addToList((Image) o);
        } else {
            String s = o.toString();
            addToList(s);
        }
    }

    private void addToList(String text) {
        TextItem textItem = new TextItem(text);
        textItem.init();
        ProjectItem projectItem = new ProjectItem(textItem);
        projectItemManager.add(0, projectItem);
    }

    private void addToList(List<File> files) {
        FileItem fileItem = new FileItem(files);
        fileItem.init();
        ProjectItem projectItem = new ProjectItem(fileItem);
        projectItemManager.add(0, projectItem);
    }

    private void addToList(File file) {
        FileItem fileItem = new FileItem(file);
        fileItem.init();
        ProjectItem projectItem = new ProjectItem(fileItem);
        projectItemManager.add(0, projectItem);
    }

    private void addToList(Image image) {
        ImageOne imageOne = new ImageOne(image);
        imageOne.init();
        ProjectItem projectItem = new ProjectItem(imageOne);
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

    private final class ProjectItemManager {

        public final List<ProjectItem> all;

        private ProjectItemManager() {
            this.all = new ArrayList<>();
        }

        public void add(ProjectItem projectItem) {
            list.getItems().add(projectItem);
            this.all.add(projectItem);
        }

        public void add(int index, ProjectItem projectItem) {
            list.getItems().add(index, projectItem);
            this.all.add(index, projectItem);
        }

        public void remove(ProjectItem projectItem) {
            list.getItems().remove(projectItem);
            this.all.remove(projectItem);
        }

        public void resetProjectItems() {
            list.getItems().clear();
            list.getItems().addAll(this.all);
        }

        public List<ProjectItem> getAll() {
            return all;
        }

        public void clear() {
            list.getItems().clear();
            this.all.clear();
        }
    }
}
