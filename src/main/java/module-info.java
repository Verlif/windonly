module idea.verlif.windonly {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires easy.language;
    requires param.parser;
    requires cmdline.parser;
    requires just.simmand;

    opens idea.verlif.windonly to javafx.fxml;
    exports idea.verlif.windonly;
    exports idea.verlif.windonly.components;
    exports idea.verlif.windonly.components.item;
    exports idea.verlif.windonly.config;
}
