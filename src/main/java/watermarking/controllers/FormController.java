package watermarking.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import watermarking.Core;
import watermarking.allignments.HorizontalAlignment;
import watermarking.allignments.VerticalAlignment;
import watermarking.providers.ImageProvider;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;

public class FormController {
    private static final Logger log = LoggerFactory.getLogger(FormController.class);

    private static final Paint DEFAULT_PAINT = Paint.valueOf("DODGERBLUE");
    private static final Paint ACTIVE_PAINT = Paint.valueOf("BLACK");

    private Stage stage;
    private Core core;

    @FXML private TextField watermarkFileNameField;
    @FXML private TextField inputFileNameField;
    @FXML private Button applyButton;
    @FXML private Button browseOriginalButton;
    @FXML private Button browseWatermarkButton;
    @FXML private Rectangle topLeft;
    @FXML private Rectangle topCenter;
    @FXML private Rectangle topRight;
    @FXML private Rectangle centerLeft;
    @FXML private Rectangle centerCenter;
    @FXML private Rectangle centerRight;
    @FXML private Rectangle bottomLeft;
    @FXML private Rectangle bottomCenter;
    @FXML private Rectangle bottomRight;

    @FXML private ImageView mainImageView;
    @FXML private ImageView watermarkImageView;

    @FXML private Label errorLabel;
    
    public void init() {
        topLeft.setFill(ACTIVE_PAINT);
        core.setVerticalAlignment(VerticalAlignment.TOP);
        core.setHorizontalAlignment(HorizontalAlignment.LEFT);
    }

    public void loadImages() {
        try {
            byte[] originalBytes = core.getBaseImageProvider().getImage();
            if (originalBytes != null) {
                log.debug("Loading original image.");
                Image originalImage = new Image(new ByteArrayInputStream(originalBytes));
                mainImageView.setImage(originalImage);
            }
        } catch (Exception e) {
            log.warn("Could not load original image", e);
        }

        try {
            byte[] watermarkBytes = core.getWatermarkImageProvider().getImage();
            if (watermarkBytes != null) {
                log.debug("Loading watermark image.");
                Image watermarkImage = new Image(new ByteArrayInputStream(watermarkBytes));
                watermarkImageView.setImage(watermarkImage);
            }
        } catch (Exception e) {
            log.warn("Could not load watermark image", e);
        }
    }

    public void loadWatermark(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose watermark image.");
        File file = chooser.showOpenDialog(stage);
        ImageProvider watermarkImageProvider = core.getWatermarkImageProvider();
        if (file != null && isImage(file)) {
            String path = file.getAbsolutePath();
            watermarkImageProvider.reset();
            watermarkFileNameField.setText(path);
            watermarkImageProvider.setSource(path);
            loadImages();
        }
    }

    public void loadOriginal(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose original image.");
        File file = chooser.showOpenDialog(stage);
        ImageProvider baseImageProvider = core.getBaseImageProvider();
        if (file != null && isImage(file)) {
            String path = file.getAbsolutePath();
            baseImageProvider.reset();
            inputFileNameField.setText(path);
            baseImageProvider.setSource(path);
            loadImages();
        }
    }

    private boolean isImage(File file) {
        return true;
    }

    public void applyWatermark(ActionEvent event) {
        errorLabel.setText("");
        try {
            core.process();
            showPreview();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private void showPreview() throws IOException {

        BufferedImage previewImage = core.getCombinedImage();
        int previewImageWidth = previewImage.getWidth();
        int previewImageHeight = previewImage.getHeight();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(previewImage, "png", outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        Stage previewStage = new Stage();
        PreviewController controller = new PreviewController(imageBytes, previewImageWidth, previewImageHeight);

        String fxmlFile = "/fxml/preview.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));

        loader.setController(controller);
        Parent rootNode = (Parent) loader.load();


        Scene scene = new Scene(rootNode, previewImageWidth, previewImageHeight);

        previewStage.setTitle("Preview");
        previewStage.setScene(scene);

        previewStage.show();
        controller.init();
    }

    public void setAlignment(MouseEvent event) {
        EventTarget target = event.getTarget();
        VerticalAlignment vAlignment = null;
        HorizontalAlignment hAlignment = null;

        if (target.equals(topLeft)) {
            vAlignment = VerticalAlignment.TOP;
            hAlignment = HorizontalAlignment.LEFT;
        }
        if (target.equals(topCenter)) {
            vAlignment = VerticalAlignment.TOP;
            hAlignment = HorizontalAlignment.CENTER;
        }
        if (target.equals(topRight)) {
            vAlignment = VerticalAlignment.TOP;
            hAlignment = HorizontalAlignment.RIGHT;
        }
        if (target.equals(centerLeft)) {
            vAlignment = VerticalAlignment.CENTER;
            hAlignment = HorizontalAlignment.LEFT;
        }
        if (target.equals(centerCenter)) {
            vAlignment = VerticalAlignment.CENTER;
            hAlignment = HorizontalAlignment.CENTER;
        }
        if (target.equals(centerRight)) {
            vAlignment = VerticalAlignment.CENTER;
            hAlignment = HorizontalAlignment.RIGHT;
        }
        if (target.equals(bottomLeft)) {
            vAlignment = VerticalAlignment.BOTTOM;
            hAlignment = HorizontalAlignment.LEFT;
        }
        if (target.equals(bottomCenter)) {
            vAlignment = VerticalAlignment.BOTTOM;
            hAlignment = HorizontalAlignment.CENTER;
        }
        if (target.equals(bottomRight)) {
            vAlignment = VerticalAlignment.BOTTOM;
            hAlignment = HorizontalAlignment.RIGHT;
        }

        core.setVerticalAlignment(vAlignment);
        core.setHorizontalAlignment(hAlignment);

        resetButtons();

        ((Rectangle)target).setFill(ACTIVE_PAINT);
    }

    private void resetButtons() {
        topLeft.setFill(DEFAULT_PAINT);
        topCenter.setFill(DEFAULT_PAINT);
        topRight.setFill(DEFAULT_PAINT);
        centerLeft.setFill(DEFAULT_PAINT);
        centerCenter.setFill(DEFAULT_PAINT);
        centerRight.setFill(DEFAULT_PAINT);
        bottomLeft.setFill(DEFAULT_PAINT);
        bottomCenter.setFill(DEFAULT_PAINT);
        bottomRight.setFill(DEFAULT_PAINT);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCore(Core core) {
        this.core = core;
    }
}
