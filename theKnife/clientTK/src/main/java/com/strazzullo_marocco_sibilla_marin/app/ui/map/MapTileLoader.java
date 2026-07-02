package com.strazzullo_marocco_sibilla_marin.app.ui.map;

import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches and caches raster tiles from the free, key-less CARTO Voyager basemap (OpenStreetMap
 * data) on {@link MapView}'s behalf, so its own redraw logic doesn't have to also deal with
 * HTTP requests, in-flight de-duplication, or caching.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class MapTileLoader {

    private static final Logger LOGGER = Logger.getLogger(MapTileLoader.class.getName());

    private static final String[] TILE_SUBDOMAINS = {"a", "b", "c", "d"};
    private static final String TILE_URL_TEMPLATE =
            "https://%s.basemaps.cartocdn.com/rastertiles/voyager/%d/%d/%d.png";

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Map<String, Image> tileCache = new ConcurrentHashMap<>();
    private final Set<String> tilesInFlight = ConcurrentHashMap.newKeySet();

    /**
     * @param zoom the tile's zoom level
     * @param wrappedX the tile's X coordinate, already wrapped to [0, 2^zoom)
     * @param y the tile's Y coordinate
     * @return the cached tile image, or null if it hasn't been fetched yet
     */
    Image get(int zoom, int wrappedX, int y) {
        return tileCache.get(key(zoom, wrappedX, y));
    }

    /**
     * Function to asynchronously fetch a tile not yet in cache. A no-op if it's already cached or
     * already has a fetch in flight.
     *
     * @param zoom the tile's zoom level
     * @param wrappedX the tile's X coordinate, already wrapped to [0, 2^zoom)
     * @param y the tile's Y coordinate
     * @param onLoaded called on the JavaFX application thread once the tile is cached
     */
    void request(int zoom, int wrappedX, int y, Runnable onLoaded) {
        String key = key(zoom, wrappedX, y);
        if (!tilesInFlight.add(key)) {
            return;
        }
        String subdomain = TILE_SUBDOMAINS[(wrappedX + y) % TILE_SUBDOMAINS.length];
        String url = String.format(TILE_URL_TEMPLATE, subdomain, zoom, wrappedX, y);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).GET().build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        tileCache.put(key, new Image(new ByteArrayInputStream(response.body())));
                        Platform.runLater(onLoaded);
                    } else {
                        LOGGER.warning("[map] tile HTTP " + response.statusCode() + " for " + url);
                    }
                })
                .exceptionally(ex -> {
                    LOGGER.log(Level.WARNING, "[map] tile fetch failed for " + url, ex);
                    return null;
                })
                .whenComplete((response, ex) -> tilesInFlight.remove(key));
    }

    /**
     * Function to build a tile's cache/in-flight key from its coordinates.
     *
     * @param zoom the tile's zoom level
     * @param x the tile's x coordinate
     * @param y the tile's y coordinate
     * @return the key
     */
    private static String key(int zoom, int x, int y) {
        return zoom + "/" + x + "/" + y;
    }
}
