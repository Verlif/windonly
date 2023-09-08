package idea.verlif.windonly.components.alert;

import idea.verlif.windonly.WindonlyApplication;
import idea.verlif.windonly.config.WindonlyConfig;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Modality;

import java.util.Optional;

public abstract class InputAlert {

    private final Alert alert;
    private final TextField inputField;

    public InputAlert(String title) {
        alert = new Alert(Alert.AlertType.INFORMATION);
        inputField = new TextField();
        alert.getDialogPane().setContent(createInput());
        alert.setResizable(false);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setTitle(title);
        alert.initOwner(WindonlyApplication.getMainStage());
        alert.initModality(Modality.APPLICATION_MODAL);
    }

    public abstract void input(String text);

    private Node createInput() {
        inputField.setPrefHeight(WindonlyConfig.getInstance().getFontSize() + 8);
        inputField.setFont(new Font(WindonlyConfig.getInstance().getFontSize()));
        inputField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                input(inputField.getText());
            }
        });
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(5));
        pane.setPrefHeight(WindonlyConfig.getInstance().getFontSize() + 8);
        pane.setCenter(inputField);
        return pane;
    }

    public void show() {
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            input(inputField.getText());
        }
    }

    public void close() {
        alert.close();
    }
}
