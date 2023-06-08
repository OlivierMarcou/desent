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
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;
import org.apache.commons.io.FileDeleteStrategy;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class MainController implements Initializable {

    final static Logger logger = Logger.getLogger(String.valueOf(MainController.class));
    static String HOME = System.getenv("HOME");

    @FXML
    private javafx.scene.control.CheckBox isOneRGB;
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

    private File saveFolder;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private void convertAllInPathAction(ActionEvent event) {
        File folder = folderChooser();
        if (folder != null) {
            createSaveFolders(folder);
            for (File fileTmp : folder.listFiles()) {
                String fileName = fileTmp.getName().toLowerCase();
                if (fileTmp.isFile()) {
                    int index = fileName.lastIndexOf(".");
                    if (index == -1)
                        continue;
                    ;
                    boolean isPng = fileName.substring(index).contains("png");
                    if (fileName.substring(index).contains("fit")
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

            createSaveFolders(file);
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
                PlanarImage image =  (PlanarImage) JAI.create("fileload", file.getAbsolutePath());
                int[] colors = new int[3];
image.getData().getPixel(0,0,colors);
                /*

//                PlanarImage image = (PlanarImage)JAI.create("fileload", _filename);
                BufferedImage image = jai.create("fileload", file);
                imageActuelle = ImageIO.read(file);
                */
                imageActuelle = ImageIO.read(file);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(imageActuelle,  "png", baos);
                byte[] bytes = baos.toByteArray();
                System.out.println("hop");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else { ,      
            imageActuelle = loadFitsFirstImage(file);
        }

        HashMap<String, WritableImage> wrs = null;
        if (isOneRGB.isSelected())
            wrs = getWritableRVBImagesOneFile(imageActuelle, file);
        else
            wrs = getWritableRVBImages(imageActuelle, file);

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
        imageViewR.setImage(wrs.get("G"));
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

    private File folderChooser() {
        this.scene = imageViewB.getScene();
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showDialog(null);
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
            wrs.put("B", new WritableImage(image.getWidth(), image.getHeight()));
            wrs.put("G", new WritableImage(image.getWidth(), image.getHeight()));
            wrs.put("R", new WritableImage(image.getWidth(), image.getHeight()));
            PixelWriter pwR = wrs.get("R").getPixelWriter();
            PixelWriter pwV = wrs.get("G").getPixelWriter();
            PixelWriter pwB = wrs.get("B").getPixelWriter();
            for (int x = 0; x < image.getWidth() - 1; x += 2) {
                for (int y = 0; y < image.getHeight() - 1; y += 2) {
                    pwR.setArgb(x, y, image.getRGB(x, y));
                    pwR.setArgb(x + 1, y, image.getRGB(x, y));
                    pwR.setArgb(x, y + 1, image.getRGB(x, y));
                    pwR.setArgb(x + 1, y + 1, image.getRGB(x, y));

                    Color c1 = new Color(image.getRGB(x + 1, y));
                    Color c2 = new Color(image.getRGB(x, y + 1));
                    int red = (c1.getRed() + c2.getRed()) / 2;
                    int green = (c1.getGreen() + c2.getGreen()) / 2;
                    int blue = (c1.getBlue() + c2.getBlue()) / 2;
                    Color finalColor = new Color(red, green, blue);
                    pwV.setArgb(x, y, finalColor.getRGB());
                    pwV.setArgb(x + 1, y, finalColor.getRGB());
                    pwV.setArgb(x, y + 1, finalColor.getRGB());
                    pwV.setArgb(x + 1, y + 1, finalColor.getRGB());

                    pwB.setArgb(x, y, image.getRGB(x + 1, y + 1));
                    pwB.setArgb(x + 1, y, image.getRGB(x + 1, y + 1));
                    pwB.setArgb(x, y + 1, image.getRGB(x + 1, y + 1));
                    pwB.setArgb(x + 1, y + 1, image.getRGB(x + 1, y + 1));
                }
            }
        }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wrs.get("B"), null),
                    "png", new File(this.saveFolder.getAbsolutePath() + File.separatorChar + "B" + File.separatorChar + "B_" + file.getName() + ".png"));
            ImageIO.write(SwingFXUtils.fromFXImage(wrs.get("G"), null),
                    "png", new File(this.saveFolder.getAbsolutePath() + File.separatorChar + "G" + File.separatorChar + "G_" + file.getName() + ".png"));
            ImageIO.write(SwingFXUtils.fromFXImage(wrs.get("R"), null),
                    "png", new File(this.saveFolder.getAbsolutePath() + File.separatorChar + "R" + File.separatorChar + "R_" + file.getName() + ".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return wrs;
    }

    private BufferedImage create48BitRGBImage(int width, int height) {
        int precision = 16; // You could in theory use less than the full 16 bits/component (ie. 12 bits/component)
        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel colorModel = new ComponentColorModel(colorSpace, new int[] {precision, precision, precision}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
        return new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(width, height), colorModel.isAlphaPremultiplied(), null);
    }
    private javafx.scene.image.Image getARGB(javafx.scene.image.Image inputImage) {
        int W = (int) inputImage.getWidth();
        int H = (int) inputImage.getHeight();
        WritableImage outputImage = new WritableImage(W, H);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int argb = reader.getArgb(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >>  8) & 0xFF;
                int b =  argb        & 0xFF;


                argb = (a << 24) | (r << 16) | (g << 8) | b;
                writer.setArgb(x, y, argb);
            }
        }

        return outputImage;
    }

    private HashMap<String, WritableImage> getWritableRVBImagesOneFile(BufferedImage image, File file) {
        HashMap<String, WritableImage> wrs = new HashMap<>();
        if (image != null) {
            wrs.put("RGB", new WritableImage(image.getWidth(), image.getHeight()));
            PixelWriter pwRGB = wrs.get("RGB").getPixelWriter();
            for (int x = 0; x < image.getWidth() - 1; x += 2) {
                for (int y = 0; y < image.getHeight() - 1; y += 2) {
                    Color r = new Color(image.getRGB(x, y));
                    Color b = new Color(image.getRGB(x + 1, y + 1));

                    Color g1 = new Color(image.getRGB(x + 1, y));
                    Color g2 = new Color(image.getRGB(x, y + 1));

                    int red = (g1.getRed() + g2.getRed()) / 2;
                    int green = (g1.getGreen() + g2.getGreen()) / 2;
                    int blue = (g1.getBlue() + g2.getBlue()) / 2;
                    Color gColor = new Color(red, green, blue);

                    Color finalColor = new Color(r.getRed(), gColor.getGreen(), b.getBlue());

                    pwRGB.setArgb(x, y, finalColor.getRGB());
                    pwRGB.setArgb(x + 1, y, finalColor.getRGB());
                    pwRGB.setArgb(x, y + 1, finalColor.getRGB());
                    pwRGB.setArgb(x + 1, y + 1, finalColor.getRGB());
                }
            }
        }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wrs.get("RGB"), null),
                    "png", new File(this.saveFolder.getAbsolutePath() + File.separatorChar + "RGB" + File.separatorChar + "R_" + file.getName() + ".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return wrs;
    }

    private void createSaveFolders(File file) {
        String filePath;
        if (file.isFile()) {
            filePath = file.getParent() + File.separatorChar + "save";
        } else {
            filePath = file.getAbsolutePath() + File.separatorChar + "save";
        }
        this.saveFolder = new File(filePath);
        try {
            FileDeleteStrategy.FORCE.delete(this.saveFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.saveFolder.mkdir();

        if (isOneRGB.isSelected()) {
            File saveRGBFolder = new File(filePath + File.separatorChar + "RGB");
            saveRGBFolder.mkdir();
        } else {
            File saveRFolder = new File(filePath + File.separatorChar + "R");
            File saveVFolder = new File(filePath + File.separatorChar + "G");
            File saveBFolder = new File(filePath + File.separatorChar + "B");
            saveRFolder.mkdir();
            saveVFolder.mkdir();
            saveBFolder.mkdir();
        }
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