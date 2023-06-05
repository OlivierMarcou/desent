package net.arkaine.desent;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.embed.swing.SwingFXUtils;
import java.awt.image.*;

import javafx.fxml.Initializable;
import javafx.scene.Scene;
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
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;

import fr.cnes.sitools.astro.cutout.FITSBufferedImage;
import edu.jhu.pha.sdss.fits.FITSImage;

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
    private Button saveAllBtn;

    @FXML
    private Button saveBtn;
    @FXML
    private ImageView imageViewB;
    @FXML
    private ImageView imageViewV;
    @FXML
    private ImageView imageViewR;

    private BufferedImage imageActuelle;
    private BufferedImage imageR;
    private BufferedImage imageV;
    private BufferedImage imageB;

    private Scene scene;

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
    private void convertAllInPathAction(ActionEvent event){
        File file = fileChooser();
        if(file != null){
            File folder = new File(file.getParent());
            List<File> imageFiles = new ArrayList<>();
            for (File fileTmp: folder.listFiles()){
                if(fileTmp.isFile()
                        && (fileTmp.getName().toLowerCase().lastIndexOf(".fit") != -1 ||
                        fileTmp.getName().toLowerCase().lastIndexOf(".fits") != -1 )){
                    readAndWriteImagesRVB(fileTmp);
                    
                } 
            }
        }
    }
    @FXML
    private void convertOneFileAction(ActionEvent event) {
        File file = fileChooser();

        if(file != null){
            readAndWriteImagesRVB(file);
        }
    }

    private void readAndWriteImagesRVB(File file) {
        if(!file.exists())
            return;

        imageActuelle = loadFitsFirstImage(file);
        HashMap<String, WritableImage> wrs = getWritableRVBImages(imageActuelle, file);
        double[] size = new double[2];
        if(imageActuelle.getHeight() > (scene.getHeight()/3-20))
            size[1] = (scene.getHeight()/3-20);
        else
            size[1] = imageActuelle.getHeight();
        if(imageActuelle.getWidth() > (scene.getWidth()/3-20))
            size[0] = (scene.getWidth()/3-20);
        else
            size[0] = imageActuelle.getWidth();
        imageViewB.setFitHeight(size[1]);
        imageViewB.setFitWidth(size[0]);
        imageViewB.setImage(wrs.get("B"));
        imageViewR.setFitHeight(size[1]);
        imageViewR.setFitWidth(size[0]);
        imageViewR.setImage(wrs.get("V"));
        imageViewV.setFitHeight(size[1]);
        imageViewV.setFitWidth(size[0]);
        imageViewV.setImage(wrs.get("R"));
    }

    private File fileChooser() {
        this.scene = pathTxt.getScene();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("FITS files (*.fit)", "*.fit");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);
        return file;
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


    private HashMap<String, WritableImage> getWritableRVBImages(BufferedImage image, File file) {
        HashMap<String, WritableImage> wrs = new HashMap<>();
        if (image != null) {
            wrs.put("B", new WritableImage(image.getWidth()/2, image.getHeight()/2));
            wrs.put("V", new WritableImage(image.getWidth()/2, image.getHeight()/2));
            wrs.put("R", new WritableImage(image.getWidth()/2, image.getHeight()/2));
            PixelWriter pwR = wrs.get("R").getPixelWriter();
            PixelWriter pwV = wrs.get("V").getPixelWriter();
            PixelWriter pwB = wrs.get("B").getPixelWriter();
            for (int x = 0; x < image.getWidth()-1; x+=2) {
                for (int y = 0; y < image.getHeight()-1; y+=2) {
                    pwR.setArgb(x/2, y/2, image.getRGB(x+1, y+1));
                    pwV.setArgb(x/2, y/2, (image.getRGB(x+1, y) + image.getRGB(x, y+1))/2);
                    pwB.setArgb(x/2, y/2, image.getRGB(x, y));
                }
            }
        }
        try {
            File saveFolder = new File(file.getParent() +File.separatorChar+ "save");
            if(!saveFolder.exists())
            {
                saveFolder.mkdir();
            }
            ImageIO.write(SwingFXUtils.fromFXImage(wrs.get("B"), null),
                    "png", new File(saveFolder.getAbsolutePath()+File.separatorChar+"b_"+file.getName()+".png"));
            ImageIO.write(SwingFXUtils.fromFXImage(wrs.get("V"), null),
                    "png", new File(saveFolder.getAbsolutePath()+File.separatorChar+"g_"+file.getName()+".png"));
            ImageIO.write(SwingFXUtils.fromFXImage(wrs.get("R"), null),
                    "png", new File(saveFolder.getAbsolutePath()+File.separatorChar+"r_"+file.getName()+".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return wrs;
    }

    private BufferedImage loadFitsFirstImage(File imagePath){

        Fits f = null;
        try {
            f = new Fits(imagePath);
        } catch (FitsException e) {
            throw new RuntimeException(e);
        }
        BasicHDU img = null;
        try {
            img = f.readHDU();
        } catch (FitsException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BufferedImage one = null;
        try {
            BufferedImage[] buf = FITSBufferedImage.createScaledImages((ImageHDU)img);
            one = buf[0];
        } catch (FitsException e) {
            throw new RuntimeException(e);
        } catch (FITSImage.DataTypeNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return one;
    }
}