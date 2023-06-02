module net.arkaine.desent {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;


    opens net.arkaine.desent to javafx.fxml;
    exports net.arkaine.desent;
}