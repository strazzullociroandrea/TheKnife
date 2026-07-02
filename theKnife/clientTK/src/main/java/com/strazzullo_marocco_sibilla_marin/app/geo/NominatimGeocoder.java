package com.strazzullo_marocco_sibilla_marin.app.geo;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
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
 * Resolves addresses to/from coordinates via the free, key-less OpenStreetMap Nominatim API (the
 * same one the server uses for server-side address distance filtering), on {@link
 * AddressGeocoder}'s behalf when no {@code GOOGLE_MAPS_API_KEY} is configured.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class NominatimGeocoder {

    private static final Logger LOGGER = Logger.getLogger(NominatimGeocoder.class.getName());
    private static final String SEARCH_URL = "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1";
    private static final String REVERSE_URL = "https://nominatim.openstreetmap.org/reverse?lat=%s&lon=%s&format=json";
    private static final String USER_AGENT = "TheKnife/1.0 (university project)";

    private final HttpClient httpClient;
    private final Gson gson;

    NominatimGeocoder(HttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    Optional<AddressGeocoder.Coordinates> geocode(String address) {
        try {
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(SEARCH_URL, encoded)))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.warning("[geocode] Nominatim HTTP " + response.statusCode() + " for address: " + address);
                return Optional.empty();
            }

            Type type = new TypeToken<List<SearchResult>>() {
            }.getType();
            List<SearchResult> results = gson.fromJson(response.body(), type);
            if (results == null || results.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(results.get(0).toCoordinates());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[geocode] failed to reach Nominatim for address: " + address, e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    Optional<String> reverseGeocode(double lat, double lng) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(Locale.ROOT, REVERSE_URL, lat, lng)))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.warning("[reverse-geocode] Nominatim HTTP " + response.statusCode() + " for " + lat + "," + lng);
                return Optional.empty();
            }
            ReverseResult result = gson.fromJson(response.body(), ReverseResult.class);
            return result == null || result.displayName == null ? Optional.empty() : Optional.of(result.displayName);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[reverse-geocode] failed to reach Nominatim", e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /**
     * Shape of a single entry in Nominatim's JSON search response, holding only the
     * fields this client cares about. Coordinates are returned as strings by the API.
     *
     * @param lat the latitude, as returned by Nominatim
     * @param lon the longitude, as returned by Nominatim
     */
    private record SearchResult(String lat, String lon) {
        AddressGeocoder.Coordinates toCoordinates() {
            return new AddressGeocoder.Coordinates(Double.parseDouble(lat), Double.parseDouble(lon));
        }
    }

    /**
     * Shape of Nominatim's reverse geocoding response, holding only the human-readable address.
     *
     * @param displayName the full formatted address, as returned by Nominatim
     */
    private record ReverseResult(@SerializedName("display_name") String displayName) {
    }
}
