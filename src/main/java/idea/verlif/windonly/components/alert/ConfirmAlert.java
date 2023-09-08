package idea.verlif.windonly.components.alert;

import idea.verlif.windonly.WindonlyApplication;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;

import java.util.Optional;

public abstract class ConfirmAlert {

    private final Alert alert;

    public ConfirmAlert(String msg) {
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(msg);
        alert.setResizable(false);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.initOwner(WindonlyApplication.getMainStage());
        alert.initModality(Modality.APPLICATION_MODAL);
    }

    public abstract void confirm();

    public void show() {
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            confirm();
        }
    }
}
