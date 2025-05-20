module dev.goral.javafximageprocessor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;

    opens dev.goral.javafximageprocessor to javafx.fxml;
    exports dev.goral.javafximageprocessor;
}