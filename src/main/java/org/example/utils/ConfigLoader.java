package org.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading test configuration from {@code test.properties} on the classpath.
 *
 * <p>Properties are loaded once at class-load time. JVM system properties (e.g. {@code -Dkey=value})
 * always take precedence over values defined in the file, enabling Docker/CI overrides.
 */
public class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final Properties props = new Properties();

    static {
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream("test.properties")) {
            if (is == null) {
                throw new IllegalStateException("test.properties not found on classpath");
            }
            props.load(is);
            log.info("Loaded test.properties from classpath");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load test.properties", e);
        }
    }

    private ConfigLoader() {
        // utility class
    }

    /**
     * Returns the string value for the given key.
     *
     * <p>JVM system properties are checked first; if absent there, the value from
     * {@code test.properties} is returned.
     *
     * @param key the property key to look up
     * @return the resolved string value for {@code key}
     * @throws IllegalStateException if {@code key} is absent from both system properties and the file
     */
    public static String get(String key) {
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }
        value = props.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value;
    }

    /**
     * Returns the boolean value for the given key by delegating to {@link #get(String)}
     * and parsing the result with {@link Boolean#parseBoolean(String)}.
     *
     * @param key the property key to look up
     * @return {@code true} if the resolved value equals {@code "true"} (case-insensitive), {@code false} otherwise
     * @throws IllegalStateException if {@code key} is absent from both system properties and the file
     */
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    /**
     * Returns the integer value for the given key by delegating to {@link #get(String)}
     * and parsing the result with {@link Integer#parseInt(String)}.
     *
     * @param key the property key to look up
     * @return the integer value associated with {@code key}
     * @throws IllegalStateException if {@code key} is absent from both system properties and the file
     * @throws NumberFormatException if the resolved value cannot be parsed as an integer
     */
    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Returns {@code true} if the given key is present in either system properties or {@code test.properties}.
     *
     * @param key the property key to check
     * @return {@code true} if the key exists, {@code false} otherwise
     */
    public static boolean hasKey(String key) {
        return System.getProperty(key) != null || props.containsKey(key);
    }
}
