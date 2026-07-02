package com.strazzullo_marocco_sibilla_marin.app.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal ".env" file reader, used as a fallback for configuration values that
 * are not exported as real OS environment variables (e.g. local development).
 * Real environment variables (set via System.getenv) always take precedence.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public final class DotEnv {

    private static final Map<String, String> FILE_VALUES = load();

    private DotEnv() {}

    /**
     * Function to resolve a configuration value, looking first at real OS
     * environment variables and then at a ".env" file in the working directory.
     *
     * @param key the variable name
     * @return the resolved value, or {@code null} if not found anywhere
     */
    public static String get(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return FILE_VALUES.get(key);
    }

    /**
     * Function to parse the {@code .env} file in the working directory into a key/value map,
     * stripping surrounding quotes from values and skipping blank lines and {@code #} comments.
     *
     * @return the parsed values, empty if the file doesn't exist or isn't readable
     */
    private static Map<String, String> load() {
        Map<String, String> values = new HashMap<>();
        Path path = Path.of(".env");
        if (!Files.isReadable(path)) {
            return values;
        }
        try {
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();
                if (value.length() >= 2 && (value.charAt(0) == '"' || value.charAt(0) == '\'')) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(key, value);
            }
        } catch (IOException e) {
            System.err.println("Unable to read .env file: " + e.getMessage());
        }
        return values;
    }
}
