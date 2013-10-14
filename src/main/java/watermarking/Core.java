package watermarking;

import org.apache.log4j.Logger;
import watermarking.allignments.Alignment;
import watermarking.allignments.HorizontalAlignment;
import watermarking.allignments.VerticalAlignment;
import watermarking.controllers.FormController;
import watermarking.providers.ImageProvider;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * @author Nikita Konovalov
 */

public class Core {
    private static final Logger log = Logger.getLogger(Core.class);

    private volatile ImageProvider baseImageProvider;
    private volatile ImageProvider watermarkImageProvider;

    private String videoInputPath;
    private String videoOutputPath;

    private HorizontalAlignment horizontalAlignment;

    private VerticalAlignment verticalAlignment;
    private volatile BufferedImage combinedImage;

    private FormController.ProgressSetter progressSetter;

    public void processImage() throws Exception {

        byte[] originalImageBytes = baseImageProvider.getImage();
        byte[] watermarkImageBytes = watermarkImageProvider.getImage();

        if (originalImageBytes == null) {
            throw new Exception("Original Image is not loaded");
        }
        if (watermarkImageBytes == null) {
            throw new Exception("WatermarkImage is not loaded");
        }

        BufferedImage original = ImageIO.read(new ByteArrayInputStream(originalImageBytes));
        BufferedImage watermark = ImageIO.read(new ByteArrayInputStream(watermarkImageBytes));

        if (original.getWidth() < watermark.getWidth()) {
            throw new Exception("Original width is smaller than watermark");
        }
        if (original.getHeight() < watermark.getHeight()) {
            throw new Exception("Original height is smaller than watermark");
        }

        BufferedImage combined = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = combined.getGraphics();
        graphics.drawImage(original, 0, 0, null);

        int offsetX = calculateOffset(original.getWidth(), watermark.getWidth(), horizontalAlignment);
        int offsetY = calculateOffset(original.getHeight(), watermark.getHeight(), verticalAlignment);

        graphics.drawImage(watermark, offsetX, offsetY, null);

        combinedImage = combined;

    }

    public void processVideo() throws Exception {
        if (videoInputPath == null) {
            throw new Exception("Video input is not specified.");
        }

        if (videoOutputPath == null) {
            throw new Exception("Video output is not specified.");
        }

        ConverterThread converterThread = new ConverterThread(this, videoInputPath, videoOutputPath);
        converterThread.start();
    }

    public void setProgress(double progress) {
        progressSetter.setProgress(progress);
    }

    private int calculateOffset(int original, int watermark, Alignment alignment) {
        if (alignment == HorizontalAlignment.LEFT || alignment == VerticalAlignment.TOP) {
            return 0;
        }
        if (alignment == HorizontalAlignment.RIGHT || alignment == VerticalAlignment.BOTTOM) {
            return original - watermark;
        }

        return original / 2 - watermark / 2;
    }

    public BufferedImage getCombinedImage() {
        return combinedImage;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public void setBaseImageProvider(ImageProvider baseImageProvider) {
        this.baseImageProvider = baseImageProvider;
    }

    public void setWatermarkImageProvider(ImageProvider watermarkImageProvider) {
        this.watermarkImageProvider = watermarkImageProvider;
    }

    public ImageProvider getBaseImageProvider() {
        return baseImageProvider;
    }

    public ImageProvider getWatermarkImageProvider() {
        return watermarkImageProvider;
    }

    public String getVideoInputPath() {
        return videoInputPath;
    }

    public void setVideoInputPath(String videoInputPath) {
        this.videoInputPath = videoInputPath;
    }

    public String getVideoOutputPath() {
        return videoOutputPath;
    }

    public void setVideoOutputPath(String videoOutputPath) {
        this.videoOutputPath = videoOutputPath;
    }

    public void setProgressSetter(FormController.ProgressSetter progressSetter) {
        this.progressSetter = progressSetter;
    }

    public FormController.ProgressSetter getProgressSetter() {
        return progressSetter;
    }
}
