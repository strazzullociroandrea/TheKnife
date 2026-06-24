package com.strazzullo_marocco_sibilla_marin.app.geo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.strazzullo_marocco_sibilla_marin.app.config.ClientConfig;
import com.strazzullo_marocco_sibilla_marin.app.geo.wifi.WifiNetwork;
import com.strazzullo_marocco_sibilla_marin.app.geo.wifi.WifiScanException;
import com.strazzullo_marocco_sibilla_marin.app.geo.wifi.WifiScanners;

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
 * Client-side resolver of a free-text address into coordinates, used to draw the
 * "Distanza" filter's reference point (user pin + radius circle) on the
 * {@link com.strazzullo_marocco_sibilla_marin.app.ui.map.MapView}. Prefers Google's Geocoding
 * API when a {@code GOOGLE_MAPS_API_KEY} is configured, falling back to the key-less
 * OpenStreetMap Nominatim API (the same one the server uses for server-side address distance
 * filtering) when it isn't, so this class works out of the box even without a Google key.
 *
 * @version 2.0
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public class AddressGeocoder {

    private static final Logger LOGGER = Logger.getLogger(AddressGeocoder.class.getName());
    private static final String NOMINATIM_SEARCH_URL = "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1";
    private static final String NOMINATIM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse?lat=%s&lon=%s&format=json";
    private static final String GOOGLE_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";
    private static final String GOOGLE_REVERSE_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=%s";
    private static final String IP_LOCATE_URL = "https://ipwho.is/";
    private static final String USER_AGENT = "TheKnife/1.0 (university project)";

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Gson gson = new Gson();

    /**
     * @param lat the resolved latitude
     * @param lng the resolved longitude
     */
    public record Coordinates(double lat, double lng) {
    }

    /**
     * Function to resolve an address into coordinates, via Google's Geocoding API if a
     * {@code GOOGLE_MAPS_API_KEY} is configured, or Nominatim otherwise.
     *
     * @param address the free-text address to resolve
     * @return the resolved coordinates, or empty if the address could not be resolved
     */
    public Optional<Coordinates> geocode(String address) {
        String apiKey = ClientConfig.googleMapsApiKey();
        return apiKey != null ? googleGeocode(address, apiKey) : nominatimGeocode(address);
    }

    /**
     * Function to resolve a human-readable address for a pair of coordinates, via Google's
     * Geocoding API if a {@code GOOGLE_MAPS_API_KEY} is configured, or Nominatim otherwise.
     * Used to turn an automatic location guess into text fed back through the normal address
     * search bar.
     *
     * @param lat the latitude to resolve
     * @param lng the longitude to resolve
     * @return the resolved address, or empty if it could not be resolved
     */
    public Optional<String> reverseGeocode(double lat, double lng) {
        String apiKey = ClientConfig.googleMapsApiKey();
        return apiKey != null ? googleReverseGeocode(lat, lng, apiKey) : nominatimReverseGeocode(lat, lng);
    }

    private Optional<Coordinates> googleGeocode(String address, String apiKey) {
        try {
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(GOOGLE_GEOCODE_URL, encoded, apiKey)))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.warning("[geocode] Google HTTP " + response.statusCode() + " for address: " + address);
                return Optional.empty();
            }
            GoogleGeocodingResponse parsed = gson.fromJson(response.body(), GoogleGeocodingResponse.class);
            if (parsed == null || parsed.results == null || parsed.results.isEmpty()) {
                return Optional.empty();
            }
            GoogleLatLng location = parsed.results.get(0).geometry.location;
            return Optional.of(new Coordinates(location.lat, location.lng));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[geocode] failed to reach Google Geocoding API for address: " + address, e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private Optional<String> googleReverseGeocode(double lat, double lng, String apiKey) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(Locale.ROOT, GOOGLE_REVERSE_GEOCODE_URL, lat, lng, apiKey)))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.warning("[reverse-geocode] Google HTTP " + response.statusCode() + " for " + lat + "," + lng);
                return Optional.empty();
            }
            GoogleGeocodingResponse parsed = gson.fromJson(response.body(), GoogleGeocodingResponse.class);
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

    private Optional<Coordinates> nominatimGeocode(String address) {
        try {
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(NOMINATIM_SEARCH_URL, encoded)))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.warning("[geocode] Nominatim HTTP " + response.statusCode() + " for address: " + address);
                return Optional.empty();
            }

            Type type = new TypeToken<List<NominatimResult>>() {
            }.getType();
            List<NominatimResult> results = gson.fromJson(response.body(), type);
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

    private Optional<String> nominatimReverseGeocode(double lat, double lng) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(Locale.ROOT, NOMINATIM_REVERSE_URL, lat, lng)))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.warning("[reverse-geocode] Nominatim HTTP " + response.statusCode() + " for " + lat + "," + lng);
                return Optional.empty();
            }
            NominatimReverseResult result = gson.fromJson(response.body(), NominatimReverseResult.class);
            return result == null || result.displayName == null
                    ? Optional.empty() : Optional.of(result.displayName);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[reverse-geocode] failed to reach Nominatim", e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /**
     * Function to guess the user's approximate position from their public IP address, via the
     * free, key-less ipwho.is lookup service, as a fallback for users who'd rather not type an
     * address just to see themselves on the map.
     *
     * @return the approximate coordinates, or empty if the lookup failed
     */
    public Optional<Coordinates> locateByIp() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(IP_LOCATE_URL))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.warning("[ip-locate] HTTP " + response.statusCode());
                return Optional.empty();
            }
            IpLocationResult result = gson.fromJson(response.body(), IpLocationResult.class);
            return result == null || !result.success
                    ? Optional.empty() : Optional.of(new Coordinates(result.latitude, result.longitude));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[ip-locate] failed to reach IP location service", e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /**
     * Function to guess the user's approximate position by scanning nearby Wi-Fi access points
     * and resolving them via Google's Geolocation API. Markedly more accurate than {@link
     * #locateByIp()}, but depends on the host OS having a usable Wi-Fi scanning tool and on a
     * configured {@code GOOGLE_MAPS_API_KEY}; either missing piece makes this return empty
     * rather than throw, so callers can treat it as just another best-effort lookup.
     *
     * @return the approximate coordinates, or empty if scanning or resolution failed
     */
    public Optional<Coordinates> locateByWifi() {
        String apiKey = ClientConfig.googleMapsApiKey();
        if (apiKey == null) {
            return Optional.empty();
        }
        try {
            List<WifiNetwork> networks = WifiScanners.forCurrentPlatform().scan();
            return new GoogleWifiGeolocationService(apiKey).locate(networks);
        } catch (WifiScanException e) {
            LOGGER.log(Level.FINE, "[wifi-locate] Wi-Fi scan unavailable", e);
            return Optional.empty();
        }
    }

    /**
     * Function to guess the user's approximate position automatically, preferring the more
     * accurate Wi-Fi-based lookup ({@link #locateByWifi()}) and falling back to the IP-based one
     * ({@link #locateByIp()}) when Wi-Fi scanning is unavailable or unconfigured.
     *
     * @return the approximate coordinates, or empty if both lookups failed
     */
    public Optional<Coordinates> locateAutomatically() {
        Optional<Coordinates> wifiLocation = locateByWifi();
        return wifiLocation.isPresent() ? wifiLocation : locateByIp();
    }

    /**
     * Shape of Google's Geocoding API response, holding only the fields this client cares about.
     * Used for both the forward (search-by-address) and reverse (search-by-coordinates) calls,
     * which share the same {@code results} envelope.
     *
     * @param results the matched locations, most relevant first
     */
    private record GoogleGeocodingResponse(List<GoogleGeocodingResult> results) {
    }

    /**
     * @param geometry the result's coordinates
     * @param formattedAddress the result's human-readable address, as returned by Google
     */
    private record GoogleGeocodingResult(
            GoogleGeometry geometry,
            @com.google.gson.annotations.SerializedName("formatted_address") String formattedAddress) {
    }

    private record GoogleGeometry(GoogleLatLng location) {
    }

    private record GoogleLatLng(double lat, double lng) {
    }

    /**
     * Shape of a single entry in Nominatim's JSON search response, holding only the
     * fields this client cares about. Coordinates are returned as strings by the API.
     *
     * @param lat the latitude, as returned by Nominatim
     * @param lon the longitude, as returned by Nominatim
     */
    private record NominatimResult(String lat, String lon) {
        Coordinates toCoordinates() {
            return new Coordinates(Double.parseDouble(lat), Double.parseDouble(lon));
        }
    }

    /**
     * Shape of Nominatim's reverse geocoding response, holding only the human-readable address.
     *
     * @param displayName the full formatted address, as returned by Nominatim
     */
    private record NominatimReverseResult(
            @com.google.gson.annotations.SerializedName("display_name") String displayName) {
    }

    /**
     * Shape of ipwho.is's JSON response, holding only the fields this client cares about.
     *
     * @param success whether the lookup succeeded
     * @param latitude the guessed latitude
     * @param longitude the guessed longitude
     */
    private record IpLocationResult(boolean success, double latitude, double longitude) {
    }
}
