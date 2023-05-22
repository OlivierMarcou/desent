module net.arkaine.desent {
    requires javafx.controls;
    requires javafx.fxml;


    opens net.arkaine.desent to javafx.fxml;
    exports net.arkaine.desent;
}