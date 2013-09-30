package watermarking.providers;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * @author Nikita Konovalov
 */

public class FileImageProvider implements ImageProvider {
    private String fileName;
    private byte[] imageBytes;

    @Override
    public void setSource(Object source) {
        this.fileName = (String) source;
    }

    @Override
    public byte[] getImage() throws IOException {
        if (imageBytes == null && fileName != null) {
            imageBytes = Files.toByteArray(new File(fileName));
        }
        return imageBytes;
    }

    @Override
    public void reset() {
        fileName = null;
        imageBytes = null;
    }
}
