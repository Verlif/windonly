module idea.verlif.windonly {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires idea.verlif.easy.dict;
    requires idea.verlif.easy.file;
    requires idea.verlif.socketpoint;
    requires com.fasterxml.jackson.databind;

    opens idea.verlif.windonly to javafx.fxml;
    exports idea.verlif.windonly;
    exports idea.verlif.windonly.components;
    exports idea.verlif.windonly.components.item;
    exports idea.verlif.windonly.config;
    exports idea.verlif.windonly.data;
    exports idea.verlif.windonly.remote;
}
