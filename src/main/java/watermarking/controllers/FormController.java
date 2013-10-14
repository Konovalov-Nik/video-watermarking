package watermarking.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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

import java.io.ByteArrayInputStream;
import java.io.File;

public class FormController {
    private static final Logger log = LoggerFactory.getLogger(FormController.class);

    private static final Paint DEFAULT_PAINT = Paint.valueOf("DODGERBLUE");
    private static final Paint ACTIVE_PAINT = Paint.valueOf("BLACK");

    private Stage stage;
    private Core core;

    @FXML private TextField watermarkFileNameField;
    @FXML private TextField originalFileNameField;
    @FXML private TextField outputFileNameField;
    @FXML private Button applyButton;
    @FXML private Button saveButton;
    @FXML private Button browseOriginalButton;
    @FXML private Button browseWatermarkButton;
    @FXML private Button browseOutputButton;
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
    @FXML private ProgressBar conversionProgressBar;

    public void init() {
        topLeft.setFill(ACTIVE_PAINT);
        core.setVerticalAlignment(VerticalAlignment.TOP);
        core.setHorizontalAlignment(HorizontalAlignment.LEFT);
        conversionProgressBar.setProgress(0.0);
        core.setDelegate(new Delegate());
    }

    public void loadImages() {
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

    public void browseOriginal(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose original video.");
        File file = chooser.showOpenDialog(stage);
        saveOriginal(file);
    }

    public void browseWatermark(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose watermark image.");
        File file = chooser.showOpenDialog(stage);
        renderAndSaveWatermark(file);
    }

    public void browseOutput(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose output file.");
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            String path = file.getAbsolutePath();
            outputFileNameField.setText(path);
            core.setVideoOutputPath(path);
        }
    }

    public void reloadOriginal(ActionEvent event) {
        File file = new File(originalFileNameField.getText());
        saveOriginal(file);
    }

    public void reloadWatermark(ActionEvent event) {
        File file = new File(watermarkFileNameField.getText());
        renderAndSaveWatermark(file);
    }

    public void setOutput(ActionEvent event) {
        core.setVideoOutputPath(outputFileNameField.getText());
    }

    public void saveOriginal(File file) {
        if (file != null && isVideo(file)) {
            String path = file.getAbsolutePath();
            originalFileNameField.setText(path);
            core.setVideoInputPath(path);
        }
    }

    public void renderAndSaveWatermark(File file) {
        ImageProvider watermarkImageProvider = core.getWatermarkImageProvider();
        if (file != null && isImage(file)) {
            String path = file.getAbsolutePath();
            watermarkImageProvider.reset();
            watermarkFileNameField.setText(path);
            watermarkImageProvider.setSource(path);
            loadImages();
        }
    }

    public void saveResultFile(ActionEvent event) {
        errorLabel.setText("");
        try {
            core.processVideo();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    public void cancelConvert() {
        core.interruptCovert();
    }


    private boolean isVideo(File file) {
        return true;
    }

    private boolean isImage(File file) {
        return true;
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

        ((Rectangle) target).setFill(ACTIVE_PAINT);
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

    public class Delegate {
        public void setProgress(double progress) {
            conversionProgressBar.setProgress(progress);
        }

        public void enableRenderButton() {
            saveButton.setDisable(false);
        }

        public void disableRenderButton() {
            saveButton.setDisable(true);
        }

        public void passException(Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
