package com.strazzullo_marocco_sibilla_marin.app.geo;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves addresses to/from coordinates via Google's Geocoding API, on {@link
 * AddressGeocoder}'s behalf when a {@code GOOGLE_MAPS_API_KEY} is configured.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class GoogleGeocoder {

    private static final Logger LOGGER = Logger.getLogger(GoogleGeocoder.class.getName());
    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";
    private static final String REVERSE_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=%s";

    private final HttpClient httpClient;
    private final Gson gson;
    private final String apiKey;

    GoogleGeocoder(HttpClient httpClient, Gson gson, String apiKey) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.apiKey = apiKey;
    }

    Optional<AddressGeocoder.Coordinates> geocode(String address) {
        try {
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(GEOCODE_URL, encoded, apiKey)))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.warning("[geocode] Google HTTP " + response.statusCode() + " for address: " + address);
                return Optional.empty();
            }
            GeocodingResponse parsed = gson.fromJson(response.body(), GeocodingResponse.class);
            if (parsed == null || parsed.results == null || parsed.results.isEmpty()) {
                return Optional.empty();
            }
            LatLng location = parsed.results.get(0).geometry.location;
            return Optional.of(new AddressGeocoder.Coordinates(location.lat, location.lng));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[geocode] failed to reach Google Geocoding API for address: " + address, e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    Optional<String> reverseGeocode(double lat, double lng) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(Locale.ROOT, REVERSE_GEOCODE_URL, lat, lng, apiKey)))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.warning("[reverse-geocode] Google HTTP " + response.statusCode() + " for " + lat + "," + lng);
                return Optional.empty();
            }
            GeocodingResponse parsed = gson.fromJson(response.body(), GeocodingResponse.class);
            return parsed == null || parsed.results == null || parsed.results.isEmpty()
                    ? Optional.empty() : Optional.of(parsed.results.get(0).formattedAddress);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[reverse-geocode] failed to reach Google Geocoding API", e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /**
     * Shape of Google's Geocoding API response, holding only the fields this client cares about.
     * Used for both the forward (search-by-address) and reverse (search-by-coordinates) calls,
     * which share the same {@code results} envelope.
     *
     * @param results the matched locations, most relevant first
     */
    private record GeocodingResponse(List<GeocodingResult> results) {
    }

    /**
     * @param geometry the result's coordinates
     * @param formattedAddress the result's human-readable address, as returned by Google
     */
    private record GeocodingResult(GoogleGeometry geometry, @SerializedName("formatted_address") String formattedAddress) {
    }

    private record GoogleGeometry(LatLng location) {
    }

    private record LatLng(double lat, double lng) {
    }
}
