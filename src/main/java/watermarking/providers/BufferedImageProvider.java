package watermarking.providers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * @author Nikita Konovalov
 */
public class BufferedImageProvider implements ImageProvider {
    private BufferedImage image;
    private byte[] cache;

    @Override
    public void setSource(Object source) {
        image = (BufferedImage) source;
    }

    @Override
    public byte[] getImage() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        cache = outputStream.toByteArray();
        return cache;
    }

    @Override
    public void reset() {
        image = null;
        cache = null;
    }
}
