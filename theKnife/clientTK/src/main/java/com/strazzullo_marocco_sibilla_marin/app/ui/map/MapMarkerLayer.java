package com.strazzullo_marocco_sibilla_marin.app.ui.map;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static com.strazzullo_marocco_sibilla_marin.app.ui.map.WebMercatorProjection.metersPerPixel;

/**
 * Owns {@link MapView}'s two non-pin markers: the "you are here" dot and the search radius
 * circle, both used by the distance filter. Screen positions depend on {@link MapView}'s camera,
 * so every marker is projected via the {@code projector} callback rather than this class knowing
 * about the camera itself.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class MapMarkerLayer {

    private final Pane overlay;
    private final BiFunction<Double, Double, double[]> projector;

    private Circle userMarker;
    private double userLat;
    private double userLng;

    private Circle radiusCircle;
    private double radiusLat;
    private double radiusLng;
    private double radiusKm = -1;

    /**
     * @param overlay the pane markers are added to
     * @param projector converts a (lat, lng) pair into its current on-screen (x, y) position
     */
    MapMarkerLayer(Pane overlay, BiFunction<Double, Double, double[]> projector) {
        this.overlay = overlay;
        this.projector = projector;
    }

    /**
     * Function to show (or move) the "you are here" marker.
     *
     * @param lat the reference point latitude
     * @param lng the reference point longitude
     */
    void setUserLocation(double lat, double lng) {
        userLat = lat;
        userLng = lng;
        if (userMarker == null) {
            userMarker = new Circle(7);
            userMarker.getStyleClass().add("tk-map-user-pin");
            userMarker.setMouseTransparent(true);
            overlay.getChildren().add(userMarker);
        }
    }

    /**
     * Function to remove the "you are here" marker, if shown.
     */
    void clearUserLocation() {
        if (userMarker != null) {
            overlay.getChildren().remove(userMarker);
            userMarker = null;
        }
    }

    /**
     * @return the user location's (lat, lng), if shown
     */
    List<double[]> userLocationPoint() {
        return userMarker == null ? List.of() : List.of(new double[]{userLat, userLng});
    }

    /**
     * Function to draw (or move/resize) the search radius circle around a reference point.
     *
     * @param lat the reference point latitude
     * @param lng the reference point longitude
     * @param radiusKm the search radius in kilometers
     */
    void setSearchRadius(double lat, double lng, double radiusKm) {
        this.radiusLat = lat;
        this.radiusLng = lng;
        this.radiusKm = radiusKm;
        if (radiusCircle == null) {
            radiusCircle = new Circle();
            radiusCircle.getStyleClass().add("tk-map-radius-circle");
            radiusCircle.setMouseTransparent(true);
            overlay.getChildren().add(0, radiusCircle);
        }
    }

    /**
     * Function to remove the search radius circle, if shown.
     */
    void clearSearchRadius() {
        if (radiusCircle != null) {
            overlay.getChildren().remove(radiusCircle);
            radiusCircle = null;
        }
        radiusKm = -1;
    }

    /**
     * @return the two corner points of the search radius circle's bounding box, for camera
     *         fitting, or empty if it isn't shown
     */
    List<double[]> radiusBoundsPoints() {
        if (radiusCircle == null) {
            return List.of();
        }
        List<double[]> points = new ArrayList<>();
        double latRadiusDeg = radiusKm / 111.0;
        double lngRadiusDeg = radiusKm / (111.0 * Math.cos(Math.toRadians(radiusLat)));
        points.add(new double[]{radiusLat + latRadiusDeg, radiusLng + lngRadiusDeg});
        points.add(new double[]{radiusLat - latRadiusDeg, radiusLng - lngRadiusDeg});
        return points;
    }

    /**
     * Function to re-project both markers to their current screen position. Called by {@link
     * MapView} on every camera change.
     *
     * @param zoom the current zoom level, needed to size the radius circle in pixels
     */
    void reposition(int zoom) {
        if (userMarker != null) {
            double[] screen = projector.apply(userLat, userLng);
            userMarker.setCenterX(screen[0]);
            userMarker.setCenterY(screen[1]);
        }
        if (radiusCircle != null) {
            double[] screen = projector.apply(radiusLat, radiusLng);
            radiusCircle.setCenterX(screen[0]);
            radiusCircle.setCenterY(screen[1]);
            radiusCircle.setRadius(radiusKm * 1000 / metersPerPixel(radiusLat, zoom));
        }
    }
}
