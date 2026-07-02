package com.strazzullo_marocco_sibilla_marin.app.geo;

import com.google.gson.Gson;
import com.strazzullo_marocco_sibilla_marin.app.config.ClientConfig;
import com.strazzullo_marocco_sibilla_marin.app.geo.wifi.WifiNetwork;
import com.strazzullo_marocco_sibilla_marin.app.geo.wifi.WifiScanException;
import com.strazzullo_marocco_sibilla_marin.app.geo.wifi.WifiScanners;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client-side resolver of a free-text address into coordinates, used to draw the
 * "Distanza" filter's reference point (user pin + radius circle) on the
 * {@link com.strazzullo_marocco_sibilla_marin.app.ui.map.MapView}. Prefers {@link GoogleGeocoder}
 * when a {@code GOOGLE_MAPS_API_KEY} is configured, falling back to the key-less {@link
 * NominatimGeocoder} (the same one the server uses for server-side address distance filtering)
 * when it isn't, so this class works out of the box even without a Google key.
 *
 * @version 3.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class AddressGeocoder {

    private static final Logger LOGGER = Logger.getLogger(AddressGeocoder.class.getName());
    private static final String IP_LOCATE_URL = "https://ipwho.is/";

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
        return apiKey != null
                ? new GoogleGeocoder(httpClient, gson, apiKey).geocode(address)
                : new NominatimGeocoder(httpClient, gson).geocode(address);
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
        return apiKey != null
                ? new GoogleGeocoder(httpClient, gson, apiKey).reverseGeocode(lat, lng)
                : new NominatimGeocoder(httpClient, gson).reverseGeocode(lat, lng);
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
     * Shape of ipwho.is's JSON response, holding only the fields this client cares about.
     *
     * @param success whether the lookup succeeded
     * @param latitude the guessed latitude
     * @param longitude the guessed longitude
     */
    private record IpLocationResult(boolean success, double latitude, double longitude) {
    }
}
