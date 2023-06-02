package net.arkaine.desent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.awt.image.BufferedImage;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import java.util.logging.Level;
import java.util.logging.Logger;


public class MainController implements Initializable {

    final static Logger logger = Logger.getLogger(String.valueOf(MainController.class));
    static String HOME = System.getenv("HOME");

    private File currentImage;

    @FXML
    private Label welcomeText;


    @FXML
    private Button desentBtn;

    @FXML
    private TextField pathTxt;

    @FXML
    private Button saveBtn;

    @FXML
    private Button loadBtn;
    @FXML
    private ImageView imageView;

    private BufferedImage imageActuelle;
    private BufferedImage imageR;
    private BufferedImage imageV;
    private BufferedImage imageB;

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


    @FXML
    private void saveAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(null);
        if(file != null){
            try {
                ImageIO.write(imageB, "png", file);
            } catch (IOException e) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, e);
//                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void loadAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("fits files (*.fit)", "*.fit");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);

        if(file != null){
            try {
                imageActuelle = ImageIO.read(file);
                Canvas canvas = new Canvas();

                WritableImage wr = getWritableImage(imageActuelle);
                imageView.setImage(wr);
             } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private WritableImage getWritableImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }
        return wr;
    }
}