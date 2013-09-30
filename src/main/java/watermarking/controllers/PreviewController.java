package watermarking.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * @author Nikita Konovalov
 */

public class PreviewController {
    private static final Logger log = LoggerFactory.getLogger(PreviewController.class);

    @FXML private ImageView preview;
    @FXML private Pane previewPane;

    private byte[] imageBytes;
    private int previewImageWidth;
    private int previewImageHeight;

    public PreviewController(byte[] imageBytes, int previewImageWidth, int previewImageHeight) {
        this.imageBytes = imageBytes;
        this.previewImageWidth = previewImageWidth;
        this.previewImageHeight = previewImageHeight;
    }

    public void init() {
        previewPane.setPrefWidth(previewImageWidth);
        previewPane.setPrefHeight(previewImageHeight);
        preview.setFitWidth(previewImageWidth);
        preview.setFitHeight(previewImageHeight);
        preview.setImage(new Image(new ByteArrayInputStream(imageBytes)));
    }
}
