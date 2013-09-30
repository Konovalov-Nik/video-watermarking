package watermarking.providers;

/**
 * @author Nikita Konovalov
 */

/*
Basic interface for retrieving an Image
 */
public interface ImageProvider {
    void setSource(Object source);
    byte[] getImage() throws Exception;
    void reset();
}
