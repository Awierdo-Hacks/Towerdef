package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Loads and caches sprite images from the {@code resources/images/} directory.
 *
 * <p>Uses the classloader to read images from the classpath so it works both when
 * running from an IDE and from a packaged JAR. Each image is loaded only once and
 * stored in a {@code HashMap} for reuse.</p>
 *
 * @author Tower Defence team
 */
public class SpriteManager {

    /** Cache of already-loaded sprites, keyed by filename. */
    private static final Map<String, BufferedImage> cache = new HashMap<>();

    /** Utility class — not meant to be instantiated. */
    private SpriteManager() {
    }

    /**
     * Returns the sprite for the given filename (e.g. {@code "floor.png"}). The image
     * is loaded on first access and cached for subsequent calls.
     *
     * @param filename the image filename within {@code resources/images/}
     * @return the loaded image, or {@code null} if it cannot be found or loaded
     */
    public static BufferedImage getSprite(String filename) {
        if (cache.containsKey(filename)) {
            return cache.get(filename);
        }

        BufferedImage image = loadImage("images/" + filename);
        cache.put(filename, image);
        return image;
    }

    private static BufferedImage loadImage(String path) {
        try (InputStream is = SpriteManager.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("[SpriteManager] Image not found: " + path);
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("[SpriteManager] Failed to load: " + path + " — " + e.getMessage());
            return null;
        }
    }
}
