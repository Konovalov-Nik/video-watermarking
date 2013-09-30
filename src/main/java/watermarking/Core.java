package watermarking;

import org.apache.log4j.Logger;
import watermarking.allignments.Alignment;
import watermarking.allignments.HorizontalAlignment;
import watermarking.allignments.VerticalAlignment;
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

    private ImageProvider baseImageProvider;
    private ImageProvider watermarkImageProvider;

    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;

    private BufferedImage combinedImage;

    public void process() throws Exception {

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
}
