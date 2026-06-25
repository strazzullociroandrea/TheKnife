package com.strazzullo_marocco_sibilla_marin.app.ui;

import com.strazzullo_marocco_sibilla_marin.app.geo.AddressGeocoder;
import com.strazzullo_marocco_sibilla_marin.app.ui.map.MapView;
import javafx.concurrent.Task;

import java.util.Optional;

/**
 * Resolves {@link SearchView}'s "Distanza" filter's reference address into coordinates and draws
 * its "you are here" pin and search radius circle on {@link MapView}, caching the last resolved
 * address so re-running a search with an unchanged address doesn't re-geocode it.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
final class SearchDistanceReference {

    private final AddressGeocoder geocoder;
    private final MapView mapView;

    private String geocodedAddress;
    private AddressGeocoder.Coordinates geocodedCoordinates;

    /**
     * @param geocoder the geocoder used to resolve the reference address
     * @param mapView the map to draw the "you are here" pin and search radius circle on
     */
    SearchDistanceReference(AddressGeocoder geocoder, MapView mapView) {
        this.geocoder = geocoder;
        this.mapView = mapView;
    }

    /**
     * Function to resolve a reference address into coordinates and update the map, or clear it
     * if the address is blank. Runs the geocoding call on a background thread, mirroring the
     * equivalent lookup the server performs to evaluate the filter itself.
     *
     * @param rawAddress the "Distanza" filter's reference address, possibly blank
     * @param radiusKm the search radius in kilometers
     */
    void update(String rawAddress, double radiusKm) {
        String address = rawAddress == null || rawAddress.isBlank() ? null : rawAddress.trim();
        if (address == null) {
            geocodedAddress = null;
            geocodedCoordinates = null;
            mapView.clearUserLocation();
            mapView.clearSearchRadius();
            return;
        }
        if (address.equals(geocodedAddress) && geocodedCoordinates != null) {
            mapView.setSearchRadius(geocodedCoordinates.lat(), geocodedCoordinates.lng(), radiusKm);
            return;
        }
        geocodedAddress = address;

        Task<Optional<AddressGeocoder.Coordinates>> task = new Task<>() {
            @Override
            protected Optional<AddressGeocoder.Coordinates> call() {
                return geocoder.geocode(address);
            }
        };
        task.setOnSucceeded(e -> task.getValue().ifPresent(coords -> {
            geocodedCoordinates = coords;
            mapView.setUserLocation(coords.lat(), coords.lng());
            mapView.setSearchRadius(coords.lat(), coords.lng(), radiusKm);
        }));

        Thread thread = new Thread(task, "geocode-worker");
        thread.setDaemon(true);
        thread.start();
    }
}
