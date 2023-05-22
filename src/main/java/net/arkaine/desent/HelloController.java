package net.arkaine.desent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {


    static String HOME = System.getenv("HOME");

    private File currentImage;

    @FXML
    private Label welcomeText;


    @FXML
    private Button desentBtn;

    @FXML
    private TextField pathTxt;

    @FXML
    protected void onHelloButtonClick() {
        this.currentImage = new File(pathTxt.getText());

        if(currentImage.isDirectory()){

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pathTxt.setText(HOME);
    }
}