package com.strazzullo_marocco_sibilla_marin.app.config;

/**
 * Resolves the optional Google Maps Platform API key the server uses for address geocoding,
 * read via {@link DotEnv}. Absence is a valid, expected state: callers must fall back to the
 * equivalent free, key-less service ({@code NominatimGeocodingService}) rather than fail.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public final class GeoConfig {

    private GeoConfig() {
    }

    /**
     * Function to resolve the Google Maps Platform API key used for server-side geocoding.
     *
     * @return the configured API key, or {@code null} if none is configured
     */
    public static String googleMapsApiKey() {
        String value = DotEnv.get("GOOGLE_MAPS_API_KEY");
        return value != null && !value.isBlank() ? value.trim() : null;
    }
}
