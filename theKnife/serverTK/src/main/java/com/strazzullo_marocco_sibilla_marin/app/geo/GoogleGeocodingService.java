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

/**
 * {@link GeocodingService} implementation backed by Google's Geocoding API, used in place of
 * {@link NominatimGeocodingService} when a {@code GOOGLE_MAPS_API_KEY} is configured, so the
 * "Distanza" filter resolves addresses with the same provider as the client's map preview.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class GoogleGeocodingService implements GeocodingService {

    private static final String SEARCH_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";

    private final HttpClient httpClient;
    private final Gson gson;
    private final String apiKey;

    /**
     * @param apiKey the Google Maps Platform API key with the Geocoding API enabled
     */
    public GoogleGeocodingService(String apiKey) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.gson = new Gson();
        this.apiKey = apiKey;
    }

    /**
     * Function to resolve an address into a geographic coordinate pair via Google's Geocoding API.
     *
     * @param address the free-text address to resolve
     * @return the resolved coordinates
     * @throws GeocodingException if the address cannot be resolved or the request fails
     */
    @Override
    public GeoPoint geocode(String address) throws GeocodingException {
        String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(SEARCH_URL, encoded, apiKey)))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new GeocodingException("Google Geocoding API returned HTTP " + response.statusCode() + " for address: " + address);
            }

            GeocodingResponse parsed = gson.fromJson(response.body(), GeocodingResponse.class);
            if (parsed == null || parsed.results == null || parsed.results.isEmpty()) {
                throw new GeocodingException("No coordinates found for address: " + address);
            }

            return parsed.results.get(0).geometry.location.toGeoPoint();
        } catch (IOException e) {
            throw new GeocodingException("Failed to reach Google Geocoding API for address: " + address, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GeocodingException("Interrupted while reaching Google Geocoding API for address: " + address, e);
        }
    }

    /**
     * Shape of Google's Geocoding API response, holding only the fields this service cares about.
     *
     * @param results the matched locations, most relevant first
     */
    private record GeocodingResponse(List<GeocodingResult> results) {
    }

    private record GeocodingResult(Geometry geometry) {
    }

    private record Geometry(@SerializedName("location") LatLng location) {
    }

    private record LatLng(double lat, double lng) {
        GeoPoint toGeoPoint() {
            return new GeoPoint(lat, lng);
        }
    }
}
