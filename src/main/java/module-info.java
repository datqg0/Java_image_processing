module duanapp.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;
    requires javafx.web;
    requires java.desktop;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.databind;
    requires javafx.swing;
    opens duanapp.main to javafx.fxml;
    exports duanapp.main;
}