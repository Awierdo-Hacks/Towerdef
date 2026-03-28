package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and provides access to game configuration from a {@code .properties} file.
 *
 * <p>Configuration files are loaded from the classpath using
 * {@link ClassLoader#getResourceAsStream(String)}, which means the file must
 * be on the classpath at runtime (e.g. inside the {@code resources/} folder
 * configured as a source root). This approach ensures the game runs on any
 * system without hardcoded file paths.</p>
 *
 * <p>All getter methods accept a {@code defaultValue} parameter that is returned
 * when the key is missing or the value cannot be parsed — this avoids null values
 * and keeps the game runnable even with an incomplete config file.</p>
 *
 * <p>Example {@code game.properties}:</p>
 * <pre>
 *   window.width=800
 *   window.height=600
 *   starting.gold=100
 *   starting.lives=20
 * </pre>
 *
 * <p>Example usage:</p>
 * <pre>
 *   ConfigManager config = new ConfigManager("config/game.properties");
 *   int width  = config.getInt("window.width", 800);
 *   int lives  = config.getInt("starting.lives", 20);
 * </pre>
 *
 * @author TowerDefence Team
 * @version 1.0
 */
public class ConfigManager {

    /** The loaded properties from the configuration file. */
    private final Properties properties;

    /**
     * Loads a properties file from the classpath.
     *
     * <p>The path is relative to the classpath root, e.g.
     * {@code "config/game.properties"} maps to
     * {@code resources/config/game.properties} if {@code resources/}
     * is a configured source root.</p>
     *
     * @param resourcePath path to the properties file on the classpath
     * @throws RuntimeException if the file cannot be found or read
     */
    public ConfigManager(String resourcePath) {
        properties = new Properties();

        // Load via classpath so the game works on any system without hardcoded paths
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new RuntimeException(
                "Configuration file not found on classpath: " + resourcePath
            );
        }

        try (InputStream is = inputStream) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to read configuration file: " + resourcePath, e
            );
        }
    }

    /**
     * Returns the value for the given key as an {@code int}.
     *
     * <p>If the key is missing or the value is not a valid integer,
     * the {@code defaultValue} is returned.</p>
     *
     * @param key          the property key
     * @param defaultValue fallback value if key is absent or unparseable
     * @return the integer value, or {@code defaultValue}
     */
    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            // Key exists but value is not a valid integer — use default
            return defaultValue;
        }
    }

    /**
     * Returns the value for the given key as a {@code double}.
     *
     * <p>If the key is missing or the value is not a valid number,
     * the {@code defaultValue} is returned.</p>
     *
     * @param key          the property key
     * @param defaultValue fallback value if key is absent or unparseable
     * @return the double value, or {@code defaultValue}
     */
    public double getDouble(String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Returns the value for the given key as a {@code String}.
     *
     * @param key          the property key
     * @param defaultValue fallback value if the key is absent
     * @return the string value, or {@code defaultValue}
     */
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Returns the value for the given key as a {@code boolean}.
     *
     * <p>Accepts {@code "true"} (case-insensitive) as {@code true};
     * everything else (including missing keys) returns the default.</p>
     *
     * @param key          the property key
     * @param defaultValue fallback value if the key is absent
     * @return {@code true} if the value equals "true" (case-insensitive),
     *         otherwise {@code defaultValue}
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }
}
