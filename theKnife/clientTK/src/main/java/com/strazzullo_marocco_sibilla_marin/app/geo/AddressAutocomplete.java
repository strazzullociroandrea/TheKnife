package com.strazzullo_marocco_sibilla_marin.app.geo;

import com.google.gson.Gson;
import com.strazzullo_marocco_sibilla_marin.app.config.ClientConfig;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Attaches address suggestions to a {@link TextField} as the user types. Prefers Google's
 * Places Autocomplete API when a {@code GOOGLE_MAPS_API_KEY} is configured, falling back to
 * Photon (photon.komoot.io) otherwise: a free, key-less, OpenStreetMap-based geocoder explicitly
 * designed for search-as-you-type, unlike Nominatim (used elsewhere in this app for one-shot
 * geocoding), whose usage policy discourages exactly this kind of rapid, per-keystroke querying.
 *
 * @version 2.0
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public final class AddressAutocomplete {

    private static final Logger LOGGER = Logger.getLogger(AddressAutocomplete.class.getName());
    private static final String PHOTON_SUGGEST_URL = "https://photon.komoot.io/api/?q=%s&limit=5";
    private static final String GOOGLE_SUGGEST_URL =
            "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=%s&key=%s";

    private static final HttpClient HTTP_CLIENT =
            HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).build();
    private static final Gson GSON = new Gson();

    private AddressAutocomplete() {
    }

    /**
     * Function to wire up debounced, asynchronous address suggestions on a text field: a short
     * pause after typing triggers a Photon lookup, and matches are shown in a dropdown the user
     * can pick from with the mouse or arrow keys.
     *
     * @param field the text field to attach suggestions to
     */
    public static void attach(TextField field) {
        ContextMenu suggestions = new ContextMenu();
        suggestions.setAutoHide(true);
        PauseTransition debounce = new PauseTransition(Duration.millis(300));

        field.textProperty().addListener((obs, oldText, newText) -> {
            debounce.stop();
            if (newText == null || newText.isBlank() || newText.length() < 3) {
                suggestions.hide();
                return;
            }
            debounce.setOnFinished(e -> fetchSuggestions(newText, field, suggestions));
            debounce.playFromStart();
        });
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                suggestions.hide();
            }
        });
    }

    private static void fetchSuggestions(String query, TextField field, ContextMenu suggestions) {
        String apiKey = ClientConfig.googleMapsApiKey();
        if (apiKey != null) {
            fetchGoogleSuggestions(query, apiKey, field, suggestions);
        } else {
            fetchPhotonSuggestions(query, field, suggestions);
        }
    }

    private static void fetchGoogleSuggestions(String query, String apiKey, TextField field, ContextMenu suggestions) {
        String url = String.format(GOOGLE_SUGGEST_URL, URLEncoder.encode(query, StandardCharsets.UTF_8), apiKey);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(5))
                .GET()
                .build();

        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        return;
                    }
                    GooglePlacesResponse parsed = GSON.fromJson(response.body(), GooglePlacesResponse.class);
                    if (parsed == null || parsed.predictions() == null) {
                        return;
                    }
                    List<String> labels = parsed.predictions().stream()
                            .map(GooglePrediction::description)
                            .filter(label -> label != null && !label.isBlank())
                            .distinct()
                            .toList();
                    Platform.runLater(() -> showSuggestions(field, suggestions, labels));
                })
                .exceptionally(ex -> {
                    LOGGER.log(Level.FINE, "[autocomplete] Google suggestion lookup failed for: " + query, ex);
                    return null;
                });
    }

    private static void fetchPhotonSuggestions(String query, TextField field, ContextMenu suggestions) {
        String url = String.format(PHOTON_SUGGEST_URL, URLEncoder.encode(query, StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(5))
                .GET()
                .build();

        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        return;
                    }
                    PhotonResponse parsed = GSON.fromJson(response.body(), PhotonResponse.class);
                    if (parsed == null || parsed.features() == null) {
                        return;
                    }
                    List<String> labels = parsed.features().stream()
                            .map(AddressAutocomplete::formatLabel)
                            .filter(label -> !label.isBlank())
                            .distinct()
                            .toList();
                    Platform.runLater(() -> showSuggestions(field, suggestions, labels));
                })
                .exceptionally(ex -> {
                    LOGGER.log(Level.FINE, "[autocomplete] Photon suggestion lookup failed for: " + query, ex);
                    return null;
                });
    }

    private static void showSuggestions(TextField field, ContextMenu suggestions, List<String> labels) {
        if (!field.isFocused() || labels.isEmpty()) {
            suggestions.hide();
            return;
        }
        suggestions.getItems().clear();
        for (String label : labels) {
            MenuItem item = new MenuItem(label);
            item.setOnAction(e -> {
                field.setText(label);
                field.positionCaret(label.length());
                suggestions.hide();
            });
            suggestions.getItems().add(item);
        }
        if (!suggestions.isShowing()) {
            suggestions.show(field, javafx.geometry.Side.BOTTOM, 0, 0);
        }
    }

    private static String formatLabel(PhotonFeature feature) {
        if (feature.properties() == null) {
            return "";
        }
        PhotonProperties p = feature.properties();
        StringBuilder label = new StringBuilder();
        appendPart(label, p.name() != null ? p.name() : p.street());
        appendPart(label, p.housenumber());
        appendPart(label, p.city());
        appendPart(label, p.country());
        return label.toString();
    }

    private static void appendPart(StringBuilder label, String part) {
        if (part == null || part.isBlank()) {
            return;
        }
        if (label.length() > 0) {
            label.append(", ");
        }
        label.append(part);
    }

    private record GooglePlacesResponse(List<GooglePrediction> predictions) {
    }

    private record GooglePrediction(String description) {
    }

    private record PhotonResponse(List<PhotonFeature> features) {
    }

    private record PhotonFeature(PhotonProperties properties) {
    }

    private record PhotonProperties(
            String name, String housenumber, String street, String city, String country) {
    }
}
