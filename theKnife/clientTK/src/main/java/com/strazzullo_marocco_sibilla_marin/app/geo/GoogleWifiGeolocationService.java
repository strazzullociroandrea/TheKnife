package com.strazzullo_marocco_sibilla_marin.app.geo;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.strazzullo_marocco_sibilla_marin.app.geo.wifi.WifiNetwork;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves a list of nearby {@link WifiNetwork} access points into an approximate position via
 * Google's Geolocation API, used as a more accurate alternative to {@link AddressGeocoder}'s
 * IP-based location guess. Unlike the rest of this app's geocoding, this backend is not free of
 * a key: it requires a Google Cloud API key with the Geolocation API enabled (configured via
 * {@code GOOGLE_MAPS_API_KEY}), so it is only used when that key is present.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class GoogleWifiGeolocationService {

    private static final Logger LOGGER = Logger.getLogger(GoogleWifiGeolocationService.class.getName());
    private static final String ENDPOINT = "https://www.googleapis.com/geolocation/v1/geolocate?key=%s";

    /**
     * When the given access points don't match anything in Google's database, the API silently
     * falls back to a coarse, city-scale estimate from the request's own IP address instead of
     * failing — and reports it with an {@code accuracy} of several kilometers rather than the
     * tens of meters a real Wi-Fi fix gets. Treating any response this imprecise as a failure
     * lets {@link AddressGeocoder#locateAutomatically()} fall through to its own, equally good,
     * free IP-based lookup instead of presenting a falsely confident wrong pin.
     */
    private static final double MAX_TRUSTED_ACCURACY_METERS = 5_000;

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Gson gson = new Gson();
    private final String apiKey;

    /**
     * @param apiKey the Google Cloud API key with the Geolocation API enabled
     */
    public GoogleWifiGeolocationService(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Function to triangulate a position from a set of observed Wi-Fi access points.
     *
     * @param networks the access points observed by a {@link com.strazzullo_marocco_sibilla_marin.app.geo.wifi.WifiScanner}
     * @return the resolved coordinates, or empty if too few access points were given or the
     *         lookup failed
     */
    public Optional<AddressGeocoder.Coordinates> locate(List<WifiNetwork> networks) {
        if (networks == null || networks.isEmpty()) {
            return Optional.empty();
        }
        try {
            String body = gson.toJson(new GeolocationRequest(networks.stream().map(WifiAccessPoint::from).toList()));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(ENDPOINT, apiKey)))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.warning("[wifi-geolocate] HTTP " + response.statusCode() + ": " + response.body());
                return Optional.empty();
            }
            GeolocationResponse parsed = gson.fromJson(response.body(), GeolocationResponse.class);
            if (parsed == null || parsed.location == null) {
                return Optional.empty();
            }
            if (parsed.accuracy > MAX_TRUSTED_ACCURACY_METERS) {
                LOGGER.fine("[wifi-geolocate] discarding low-accuracy fix (" + parsed.accuracy
                        + "m), likely an IP-based fallback rather than a real Wi-Fi match");
                return Optional.empty();
            }
            return Optional.of(new AddressGeocoder.Coordinates(parsed.location.lat, parsed.location.lng));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[wifi-geolocate] failed to reach Google Geolocation API", e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /**
     * Shape of one access point in Google's Geolocation API request body.
     *
     * @param macAddress the access point's BSSID
     * @param signalStrength the observed signal strength in dBm, or null if unknown
     * @param channel the Wi-Fi channel, or null if unknown
     */
    private record WifiAccessPoint(
            @SerializedName("macAddress") String macAddress,
            @SerializedName("signalStrength") Integer signalStrength,
            @SerializedName("channel") Integer channel) {

        static WifiAccessPoint from(WifiNetwork network) {
            return new WifiAccessPoint(network.bssid(), network.signalStrengthDbm(), network.channel());
        }
    }

    private record GeolocationRequest(
            @SerializedName("wifiAccessPoints") List<WifiAccessPoint> wifiAccessPoints) {
    }

    private static final class GeolocationResponse {
        private GeolocationLocation location;
        private double accuracy;
    }

    private static final class GeolocationLocation {
        private double lat;
        private double lng;
    }
}
