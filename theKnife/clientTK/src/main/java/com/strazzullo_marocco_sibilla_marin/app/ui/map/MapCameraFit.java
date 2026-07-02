package com.strazzullo_marocco_sibilla_marin.app.ui.map;

import java.util.List;

import static com.strazzullo_marocco_sibilla_marin.app.ui.map.WebMercatorProjection.latToWorldY;
import static com.strazzullo_marocco_sibilla_marin.app.ui.map.WebMercatorProjection.lonToWorldX;

/**
 * Computes the camera state ({@link MapCamera}'s center/zoom) that fits a set of geographic
 * points on screen with some padding, for {@link MapCamera#fitToPointsInstant} and {@link
 * MapCamera#fitToPointsAnimated}.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class MapCameraFit {

    private static final int MIN_ZOOM = 3;
    private static final int FIT_MAX_ZOOM = 15;
    private static final double FIT_PADDING_PX = 40;

    private MapCameraFit() {
    }

    /**
     * @param lat the latitude to center on
     * @param lng the longitude to center on
     * @param zoom the zoom level fitting every point with some padding
     */
    record CameraTarget(double lat, double lng, int zoom) {
    }

    /**
     * Function to compute the camera state that fits a set of points on screen with some padding,
     * picking the highest zoom level at which they all still fit.
     *
     * @param points the points to fit, in (lat, lng) pairs
     * @param canvasWidth the canvas width to fit within
     * @param canvasHeight the canvas height to fit within
     * @return the fitting camera state, or null if the canvas has no real size yet or there are
     *         no points to fit
     */
    static CameraTarget compute(List<double[]> points, double canvasWidth, double canvasHeight) {
        if (points.isEmpty() || canvasWidth <= 0 || canvasHeight <= 0) {
            return null;
        }

        double minLat = points.get(0)[0];
        double maxLat = minLat;
        double minLng = points.get(0)[1];
        double maxLng = minLng;
        for (double[] point : points) {
            minLat = Math.min(minLat, point[0]);
            maxLat = Math.max(maxLat, point[0]);
            minLng = Math.min(minLng, point[1]);
            maxLng = Math.max(maxLng, point[1]);
        }

        double fitLat = (minLat + maxLat) / 2;
        double fitLng = (minLng + maxLng) / 2;

        if (points.size() == 1) {
            return new CameraTarget(fitLat, fitLng, FIT_MAX_ZOOM);
        }

        int fitZoom = MIN_ZOOM;
        for (int z = FIT_MAX_ZOOM; z >= MIN_ZOOM; z--) {
            double spanX = lonToWorldX(maxLng, z) - lonToWorldX(minLng, z);
            double spanY = Math.abs(latToWorldY(maxLat, z) - latToWorldY(minLat, z));
            if (spanX <= canvasWidth - 2 * FIT_PADDING_PX && spanY <= canvasHeight - 2 * FIT_PADDING_PX) {
                fitZoom = z;
                break;
            }
        }
        return new CameraTarget(fitLat, fitLng, fitZoom);
    }
}
