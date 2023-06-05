module net.arkaine.desent {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;
    requires nom.tam.fits;
    requires javafx.swing;
    requires org.apache.commons.io;


    opens net.arkaine.desent to javafx.fxml;
    exports net.arkaine.desent;
}