module dev.goral.javafximageprocessor {
    requires javafx.controls;
    requires javafx.fxml;


    opens dev.goral.javafximageprocessor to javafx.fxml;
    exports dev.goral.javafximageprocessor;
}