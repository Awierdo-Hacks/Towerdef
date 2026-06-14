package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/*
 * Loads and caches sprite images from the resources/images/ directory.
 *
 * Uses the classloader to read images from the classpath so it works
 * both when running from an IDE and from a packaged JAR.
 *
 * Each image is loaded only once and stored in a HashMap for reuse.
 */
public class SpriteManager {

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    /*
     * Returns the sprite for the given filename (e.g. "floor.png").
     * Loads the image on first access and caches it for subsequent calls.
     * Returns null if the image cannot be found or loaded.
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
