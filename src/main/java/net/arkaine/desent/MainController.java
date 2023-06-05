package net.arkaine.desent;


import edu.jhu.pha.sdss.fits.FITSImage;
import fr.cnes.sitools.astro.cutout.FITSBufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class MainController implements Initializable {

    final static Logger logger = Logger.getLogger(String.valueOf(MainController.class));
    static String HOME = System.getenv("HOME");

    @FXML
    private Label welcomeText;
    @FXML
    private ImageView imageViewB;
    @FXML
    private ImageView imageViewV;
    @FXML
    private ImageView imageViewR;

    private BufferedImage imageActuelle;

    private Scene scene;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private void convertAllInPathAction(ActionEvent event) {
        File file = fileChooser();
        if (file != null) {
            File folder = new File(file.getParent());
            List<File> imageFiles = new ArrayList<>();
            for (File fileTmp : folder.listFiles()) {
                String fileName = fileTmp.getName().toLowerCase();
                if (fileTmp.isFile()) {
                    boolean isPng = fileName.substring(fileName.lastIndexOf(".")).contains("png");
                    if (fileName.substring(fileName.lastIndexOf(".")).contains("fit")
                            || isPng) {
                        readAndWriteImagesRVB(fileTmp, isPng);
                    }

                }
            }
        }
    }

    @FXML
    private void convertOneFileAction(ActionEvent event) {
        File file = fileChooser();

        if (file != null) {

            if (file.isFile()) {
                String fileName = file.getName().toLowerCase();
                boolean isPng = fileName.substring(fileName.lastIndexOf(".")).contains("png");
                if (fileName.substring(fileName.lastIndexOf(".")).contains("fit")
                        || isPng) {
                    readAndWriteImagesRVB(file, isPng);
                }

            }
        }
    }

    private void readAndWriteImagesRVB(File file, boolean isPng) {
        if (!file.exists())
            return;

        if (isPng) {
            try {
                imageActuelle = ImageIO.read(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            imageActuelle = loadFitsFirstImage(file);
        }

        HashMap<String, WritableImage> wrs = getWritableRVBImages(imageActuelle, file);
        double[] size = new double[2];
        if (imageActuelle.getHeight() > (scene.getHeight() / 3 - 20))
            size[1] = (scene.getHeight() / 3 - 20);
        else
            size[1] = imageActuelle.getHeight();
        if (imageActuelle.getWidth() > (scene.getWidth() / 3 - 20))
            size[0] = (scene.getWidth() / 3 - 20);
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
        this.scene = imageViewB.getScene();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("FITS files (*.fit)", "*.fit");
        fileChooser.getExtensionFilters().add(extFilter);
        extFilter =
                new FileChooser.ExtensionFilter("FITS files (*.png)", "*.png");
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
            wrs.put("B", new WritableImage(image.getWidth() / 2, image.getHeight() / 2));
            wrs.put("V", new WritableImage(image.getWidth() / 2, image.getHeight() / 2));
            wrs.put("R", new WritableImage(image.getWidth() / 2, image.getHeight() / 2));
            PixelWriter pwR = wrs.get("R").getPixelWriter();
            PixelWriter pwV = wrs.get("V").getPixelWriter();
            PixelWriter pwB = wrs.get("B").getPixelWriter();
            for (int x = 0; x < image.getWidth() - 1; x += 2) {
                for (int y = 0; y < image.getHeight() - 1; y += 2) {
                    pwR.setArgb(x / 2, y / 2, image.getRGB(x, y));

                    Color c1 = new Color(image.getRGB(x + 1, y));
                    int red = c1.getRed();
                    int green = c1.getGreen();
                    int blue = c1.getBlue();

                    Color c2 = new Color(image.getRGB(x, y + 1));
                    red = (c1.getRed() + c2.getRed()) / 2;
                    green = (c1.getGreen() + c2.getGreen()) / 2;
                    blue = (c1.getBlue() + c2.getBlue()) / 2;

                    Color finalColor = new Color(red, green, blue);

                    pwV.setArgb(x / 2, y / 2, finalColor.getRGB());

                    pwB.setArgb(x / 2, y / 2, image.getRGB(x + 1, y + 1));
                }
            }
        }
        try {
            File saveFolder = new File(file.getParent() + File.separatorChar + "save");
            if (!saveFolder.exists()) {
                saveFolder.mkdir();
            }
            ImageIO.write(SwingFXUtils.fromFXImage(wrs.get("B"), null),
                    "png", new File(saveFolder.getAbsolutePath() + File.separatorChar + "b_" + file.getName() + ".png"));
            ImageIO.write(SwingFXUtils.fromFXImage(wrs.get("V"), null),
                    "png", new File(saveFolder.getAbsolutePath() + File.separatorChar + "g_" + file.getName() + ".png"));
            ImageIO.write(SwingFXUtils.fromFXImage(wrs.get("R"), null),
                    "png", new File(saveFolder.getAbsolutePath() + File.separatorChar + "r_" + file.getName() + ".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return wrs;
    }

    private BufferedImage loadFitsFirstImage(File imagePath) {

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
            BufferedImage[] buf = FITSBufferedImage.createScaledImages((ImageHDU) img);
            one = buf[0];
        } catch (FitsException e) {
            throw new RuntimeException(e);
        } catch (FITSImage.DataTypeNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return one;
    }
}