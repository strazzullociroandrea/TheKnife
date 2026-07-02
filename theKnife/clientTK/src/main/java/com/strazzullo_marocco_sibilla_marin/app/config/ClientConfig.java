package com.strazzullo_marocco_sibilla_marin.app.config;

/**
 * Resolves the RMI connection settings the client UI uses to reach the TheKnife server,
 * read from real OS environment variables, falling back to sensible local defaults.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public final class ClientConfig {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1099;

    private ClientConfig() {}

    /**
     * Function to resolve the RMI registry host.
     *
     * @return the value of the {@code RMI_HOST} environment variable, or {@code "localhost"} if unset
     */
    public static String rmiHost() {
        String value = System.getenv("RMI_HOST");
        return value != null && !value.isBlank() ? value.trim() : DEFAULT_HOST;
    }

    /**
     * Function to resolve the RMI registry port.
     *
     * @return the value of the {@code RMI_PORT} environment variable, or {@code 1099} if unset or invalid
     */
    public static int rmiPort() {
        String value = System.getenv("RMI_PORT");
        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }

    /**
     * Function to resolve the API key shared by the Google Maps Platform APIs this app uses
     * (Geolocation, Geocoding, and Places Autocomplete). Looked up via {@link DotEnv}, so it can
     * come from either a real environment variable or a ".env" file in the working directory.
     * Every caller of this key must treat its absence as "fall back to the equivalent free,
     * key-less service" rather than failing, since the key is optional.
     *
     * @return the configured API key, or {@code null} if no Google Maps Platform key is configured
     */
    public static String googleMapsApiKey() {
        String value = DotEnv.get("GOOGLE_MAPS_API_KEY");
        return value != null && !value.isBlank() ? value.trim() : null;
    }
}
