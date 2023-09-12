module idea.verlif.windonly {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires easy.language;
    requires easy.file;
    requires socket.point;
    requires com.fasterxml.jackson.databind;
    requires jintellitype;

    opens idea.verlif.windonly to javafx.fxml;
    exports idea.verlif.windonly;
    exports idea.verlif.windonly.components;
    exports idea.verlif.windonly.components.item;
    exports idea.verlif.windonly.config;
    exports idea.verlif.windonly.data;
    exports idea.verlif.windonly.remote;
}
