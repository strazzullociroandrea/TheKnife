package com.strazzullo_marocco_sibilla_marin.app.geo;

import com.google.gson.Gson;
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
import java.util.Map;

/**
 * {@link GeocodingService} implementation backed by the public OpenStreetMap
 * Nominatim search API. No API key is required, but every request must carry
 * an identifying User-Agent per Nominatim's usage policy.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class NominatimGeocodingService implements GeocodingService {

    private static final String SEARCH_URL = "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1";
    private static final String USER_AGENT = "TheKnife/1.0 (university project)";

    private final HttpClient httpClient;
    private final Gson gson;

    /**
     * NominatimGeocodingService constructor.
     */
    public NominatimGeocodingService() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.gson = new Gson();
    }

    /**
     * Function to resolve an address into a geographic coordinate pair via Nominatim.
     *
     * @param address the free-text address to resolve
     * @return the resolved coordinates
     * @throws GeocodingException if the address cannot be resolved or the request fails
     */
    @Override
    public GeoPoint geocode(String address) throws GeocodingException {
        String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(SEARCH_URL, encoded)))
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new GeocodingException("Geocoding service returned HTTP " + response.statusCode() + " for address: " + address);
            }

            Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> results = gson.fromJson(response.body(), type);
            if (results == null || results.isEmpty()) {
                throw new GeocodingException("No coordinates found for address: " + address);
            }

            Map<String, Object> first = results.get(0);
            double latitude = Double.parseDouble(String.valueOf(first.get("lat")));
            double longitude = Double.parseDouble(String.valueOf(first.get("lon")));
            return new GeoPoint(latitude, longitude);
        } catch (IOException | InterruptedException e) {
            throw new GeocodingException("Failed to reach geocoding service for address: " + address, e);
        }
    }
}
