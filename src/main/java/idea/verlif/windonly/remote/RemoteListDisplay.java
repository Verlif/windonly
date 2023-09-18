package idea.verlif.windonly.remote;

import idea.verlif.windonly.WindonlyApplication;
import idea.verlif.windonly.components.alert.ConfirmAlert;
import idea.verlif.windonly.components.alert.InputAlert;
import idea.verlif.windonly.config.RemoteConfig;
import idea.verlif.windonly.utils.MessageUtil;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

import java.util.List;

public class RemoteListDisplay {

    private final TextInputDialog dialog;
    private final ListView<Label> remoteList;

    public RemoteListDisplay() {
        this.dialog = new TextInputDialog();
        this.remoteList = new ListView<>();
        dialog.setResizable(true);
        dialog.setHeaderText(null);
        dialog.setGraphic(null);
        dialog.setTitle("");
        dialog.initOwner(WindonlyApplication.getMainStage());
        dialog.initModality(Modality.NONE);
        init();
    }

    private void init() {
        ObservableList<ButtonType> buttonTypes = dialog.getDialogPane().getButtonTypes();
        buttonTypes.remove(ButtonType.OK);
        buttonTypes.remove(ButtonType.CANCEL);
        buttonTypes.add(ButtonType.CLOSE);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(0, 5, 0, 5));
        pane.setPrefHeight(200);
        pane.setCenter(remoteList);
        dialog.getDialogPane().setContent(pane);
        dialog.setOnShown(dialogEvent -> refreshList());
    }

    private void refreshList() {
        remoteList.getItems().clear();
        Label label = new Label(MessageUtil.get("newRemote"));
        label.setOnMouseClicked(actionEvent -> {
            new InputAlert(MessageUtil.get("newRemote")) {
                @Override
                public void input(String text) {
                    RemoteConfig.getInstance().getIpData().add(new RemoteConfig.IpData(text));
                    RemoteConfig.getInstance().saveToFile();
                    refreshList();
                }
            }.show();
        });
        remoteList.getItems().add(label);

        List<RemoteConfig.IpData> ipData = RemoteConfig.getInstance().getIpData();
        for (RemoteConfig.IpData ipDatum : ipData) {
            remoteList.getItems().add(ipDataLabel(ipDatum));
        }
    }

    private Label ipDataLabel(RemoteConfig.IpData ipData) {
        Label label = new Label(ipData.getIp() + ":" + ipData.getPort());

        // 删除远程
        MenuItem delRemote = new MenuItem(MessageUtil.get("delRemote"));
        delRemote.setOnAction(actionEvent -> {
            new ConfirmAlert(MessageUtil.get("delRemote")) {
                @Override
                public void confirm() {
                    RemoteConfig.getInstance().getIpData().remove(ipData);
                    RemoteConfig.getInstance().saveToFile();
                    refreshList();
                }
            }.show();
        });

        label.setContextMenu(new ContextMenu(delRemote));
        return label;
    }

    public void show() {
        dialog.show();
    }

    public void close() {
        dialog.close();
    }
}
