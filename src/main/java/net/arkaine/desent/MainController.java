package net.arkaine.desent;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.awt.color.ColorSpace;
import java.awt.image.*;

import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;

import edu.jhu.pha.sdss.fits.Histogram;
import edu.jhu.pha.sdss.fits.ScaleUtils;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.ResourceBundle;

import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;
import nom.tam.util.ArrayDataInput;

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
    private Button saveBtn;

    @FXML
    private Button loadBtn;
    @FXML
    private ImageView imageView;

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
        this.scene = pathTxt.getScene();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("FITS files (*.fit)", "*.fit");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);

        if(file != null){
            try {
                imageActuelle = ImageIO.read(file);
                Canvas canvas = new Canvas();

                WritableImage wr = getWritableImage(imageActuelle);
                double[] size = new double[2];
                if(imageActuelle.getHeight() > (scene.getHeight()-20))
                    size[1] = (scene.getHeight()-20);
                else
                    size[1] = imageActuelle.getHeight();
                if(imageActuelle.getWidth() > (scene.getWidth()-20))
                    size[0] = (scene.getWidth()-20);
                else
                    size[0] = imageActuelle.getWidth();
                imageView.setFitHeight(size[1]);
                imageView.setFitWidth(size[0]);
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

    private Image loadFitsFirstImage(){

        Fits f = null;
        try {
            f = new Fits("bigimg.fits");
        } catch (FitsException e) {
            throw new RuntimeException(e);
        }
        BasicHDU img = null;
        try {
            img = f.getHDU(0);
        } catch (FitsException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (img.getData().reset()) {
            int[] line = new int[100000];
            long sum   = 0;
            long count = 0;
            ArrayDataInput in = f.getStream();
            while (true) {
                try {
                    if (!(in.readLArray(line) == line.length * 4)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }  // int is 4 bytes
                for (int i=0; i<line.length; i += 1) {
                    sum += line[i];
                    count++;
                }
            }
            double avg = ((double) sum)/count;
        } else {
            System.err.println("Unable to seek to data");
        }
        BufferedImage buf = FITSBufferedImage.createScaledImages(img);
        return new Image("0,0,0");
    }


    public static BufferedImage[] createScaledImages(ImageHDU hdu, int dataCubeIndex) throws FitsException, FITSImage.DataTypeNotSupportedException {
        int bitpix = hdu.getBitPix();
        double bZero = hdu.getBZero();
        double bScale = hdu.getBScale();
        int[] naxes = hdu.getAxes();
        int width = naxes.length == 3 ? hdu.getAxes()[2] : hdu.getAxes()[1];
        int height = naxes.length == 3 ? hdu.getAxes()[1] : hdu.getAxes()[0];
        Object data = hdu.getData().getData();
        Histogram hist = null;
        short[][] scaledData = (short[][]) null;
        Object cimage = data;
        switch (bitpix) {
            case 8:
                hist = ScaleUtils.computeHistogram((byte[][]) (byte[][]) cimage, bZero, bScale);
                scaledData = ScaleUtils.scaleToUShort((byte[][]) (byte[][]) cimage, hist, width, height, bZero, bScale, hist.getMin(), hist.getMax(), hist.estimateSigma());

                break;
            case 16:
                hist = ScaleUtils.computeHistogram((short[][]) (short[][]) cimage, bZero, bScale);
                scaledData = ScaleUtils.scaleToUShort((short[][]) (short[][]) cimage, hist, width, height, bZero, bScale, hist.getMin(), hist.getMax(), hist.estimateSigma());

                break;
            case 32:
                hist = ScaleUtils.computeHistogram((int[][]) (int[][]) cimage, bZero, bScale);
                scaledData = ScaleUtils.scaleToUShort((int[][]) (int[][]) cimage, hist, width, height, bZero, bScale, hist.getMin(), hist.getMax(), hist.estimateSigma());

                break;
            case -32:
                hist = ScaleUtils.computeHistogram((float[][]) (float[][]) cimage, bZero, bScale);
                scaledData = ScaleUtils.scaleToUShort((float[][]) (float[][]) cimage, hist, width, height, bZero, bScale, hist.getMin(), hist.getMax(), hist.estimateSigma());

                break;
            case -64:
                hist = ScaleUtils.computeHistogram((double[][]) (double[][]) cimage, bZero, bScale);
                scaledData = ScaleUtils.scaleToUShort((double[][]) (double[][]) cimage, hist, width, height, bZero, bScale, hist.getMin(), hist.getMax(), hist.estimateSigma());

                break;
            default:
                throw new FITSImage.DataTypeNotSupportedException(bitpix);
        }

        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(1000), false, false, 1, 1);

        SampleModel sm = cm.createCompatibleSampleModel(width, height);

        Hashtable properties = new Hashtable();
        properties.put("histogram", hist);
        properties.put("imageHDU", hdu);

        BufferedImage[] result = new BufferedImage[scaledData.length];

        for (int i = 0; i < result.length; i++) {
            DataBuffer db = new DataBufferUShort(scaledData[i], height);
            WritableRaster r = Raster.createWritableRaster(sm, db, null);

            result[i] = new BufferedImage(cm, r, false, properties);
        }

        return result;
    }

}