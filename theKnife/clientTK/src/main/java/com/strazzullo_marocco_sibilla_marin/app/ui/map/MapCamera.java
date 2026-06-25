package com.strazzullo_marocco_sibilla_marin.app.ui.map;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.scene.canvas.Canvas;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.util.List;

import static com.strazzullo_marocco_sibilla_marin.app.ui.map.WebMercatorProjection.latToWorldY;
import static com.strazzullo_marocco_sibilla_marin.app.ui.map.WebMercatorProjection.lonToWorldX;
import static com.strazzullo_marocco_sibilla_marin.app.ui.map.WebMercatorProjection.worldXToLon;
import static com.strazzullo_marocco_sibilla_marin.app.ui.map.WebMercatorProjection.worldYToLat;

/**
 * Owns {@link MapView}'s camera state (center latitude/longitude and zoom) and every way it
 * changes: direct panning, scroll/double-click zoom, and the eased animation used by every
 * programmatic change (search recenter, focusing a pin, the zoom buttons). Also does the
 * geographic/screen coordinate conversions that depend on that state, since they're meaningless
 * without it.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
final class MapCamera {

    static final double ITALY_LAT = 41.9;
    static final double ITALY_LNG = 12.5;
    static final int ITALY_ZOOM = 6;

    private static final int MIN_ZOOM = 3;
    private static final int MAX_ZOOM = 18;

    /**
     * Duration of the eased transition used whenever the camera changes programmatically. Direct
     * mouse drag/scroll stay real-time/un-animated, since they're already driven frame-by-frame
     * by the user's own input.
     */
    private static final double CAMERA_ANIMATION_MILLIS = 320;

    private final Canvas canvas;

    /**
     * Applied to the map's tile layer to fake a continuous zoom while an animation is in flight:
     * tiles are drawn once at the animation's starting integer zoom level and this transform
     * scales them smoothly towards the target level's apparent size, instead of
     * re-fetching/redrawing tiles every frame. Reset to identity once the animation lands.
     */
    private final Scale zoomScale;
    private final Runnable onChanged;

    private double centerLat = ITALY_LAT;
    private double centerLng = ITALY_LNG;
    private int zoom = ITALY_ZOOM;

    private Animation cameraAnimation;
    private double animatingToLat;
    private double animatingToLng;
    private int animatingToZoom;

    /**
     * @param canvas the map's tile canvas, used for its current width/height
     * @param zoomScale the transform animated to fake zoom transitions
     * @param onChanged called every time the camera's state changes, to trigger a repaint
     */
    MapCamera(Canvas canvas, Scale zoomScale, Runnable onChanged) {
        this.canvas = canvas;
        this.zoomScale = zoomScale;
        this.onChanged = onChanged;
    }

    double centerLat() {
        return centerLat;
    }

    double centerLng() {
        return centerLng;
    }

    int zoom() {
        return zoom;
    }

    /**
     * @return the current center, in world pixel coordinates at the current zoom
     */
    double[] centerWorld() {
        return new double[]{lonToWorldX(centerLng, zoom), latToWorldY(centerLat, zoom)};
    }

    /**
     * Function to instantly re-center the camera from world pixel coordinates, with no animation.
     * Used by mouse drag panning, which is already driven frame-by-frame by the user's own input.
     *
     * @param worldX the new center's X world coordinate, at the current zoom
     * @param worldY the new center's Y world coordinate, at the current zoom
     */
    void panToWorld(double worldX, double worldY) {
        centerLng = worldXToLon(worldX, zoom);
        centerLat = worldYToLat(worldY, zoom);
        onChanged.run();
    }

    /**
     * Function to zoom in/out by one level, keeping the point currently under the cursor fixed
     * on screen, animated.
     *
     * @param screenX the cursor's X position on the canvas
     * @param screenY the cursor's Y position on the canvas
     * @param direction +1 to zoom in, -1 to zoom out
     */
    void zoomAtScreenPoint(double screenX, double screenY, int direction) {
        int newZoom = clamp(zoom + direction, MIN_ZOOM, MAX_ZOOM);
        if (newZoom == zoom) {
            return;
        }
        double[] underCursor = screenToLatLng(screenX, screenY);
        double targetWorldX = lonToWorldX(underCursor[1], newZoom);
        double targetWorldY = latToWorldY(underCursor[0], newZoom);
        double newCenterWorldX = targetWorldX - (screenX - canvas.getWidth() / 2);
        double newCenterWorldY = targetWorldY - (screenY - canvas.getHeight() / 2);
        animateTo(worldYToLat(newCenterWorldY, newZoom), worldXToLon(newCenterWorldX, newZoom), newZoom);
    }

    /**
     * Function to glide the camera to a new center/zoom instead of jump-cutting. Tiles are drawn
     * once at the animation's starting zoom level and {@link #zoomScale} fakes the rest of the
     * zoom transition, so no extra tile fetches happen mid-animation; the target zoom's real
     * tiles are only fetched once the camera lands.
     *
     * @param targetLat the latitude to end up centered on
     * @param targetLng the longitude to end up centered on
     * @param targetZoom the zoom level to end up at
     */
    void animateTo(double targetLat, double targetLng, int targetZoom) {
        cancelAnimation();
        double startLat = centerLat;
        double startLng = centerLng;
        int startZoom = zoom;
        if (startLat == targetLat && startLng == targetLng && startZoom == targetZoom) {
            return;
        }
        double targetScale = Math.pow(2, targetZoom - startZoom);
        zoomScale.setPivotX(canvas.getWidth() / 2);
        zoomScale.setPivotY(canvas.getHeight() / 2);

        Transition transition = new Transition() {
            {
                setCycleDuration(Duration.millis(CAMERA_ANIMATION_MILLIS));
            }

            @Override
            protected void interpolate(double frac) {
                centerLat = startLat + (targetLat - startLat) * frac;
                centerLng = startLng + (targetLng - startLng) * frac;
                double scale = 1 + (targetScale - 1) * frac;
                zoomScale.setX(scale);
                zoomScale.setY(scale);
                onChanged.run();
            }
        };
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.setOnFinished(e -> finalizeTo(targetLat, targetLng, targetZoom));

        animatingToLat = targetLat;
        animatingToLng = targetLng;
        animatingToZoom = targetZoom;
        cameraAnimation = transition;
        transition.play();
    }

    /**
     * Function to immediately settle any in-flight animation at the state it was animating
     * towards. Called before starting a new animation and before a user drag takes over, so
     * neither ever has to deal with a mid-animation {@link #zoomScale}.
     */
    void cancelAnimation() {
        if (cameraAnimation == null) {
            return;
        }
        cameraAnimation.stop();
        finalizeTo(animatingToLat, animatingToLng, animatingToZoom);
    }

    private void finalizeTo(double lat, double lng, int targetZoom) {
        centerLat = lat;
        centerLng = lng;
        zoom = targetZoom;
        zoomScale.setX(1);
        zoomScale.setY(1);
        cameraAnimation = null;
        onChanged.run();
    }

    double[] screenToLatLng(double screenX, double screenY) {
        double worldX = lonToWorldX(centerLng, zoom) + (screenX - canvas.getWidth() / 2);
        double worldY = latToWorldY(centerLat, zoom) + (screenY - canvas.getHeight() / 2);
        return new double[]{worldYToLat(worldY, zoom), worldXToLon(worldX, zoom)};
    }

    double[] projectToScreen(double lat, double lng) {
        double worldX = lonToWorldX(lng, zoom);
        double worldY = latToWorldY(lat, zoom);
        double centerWorldX = lonToWorldX(centerLng, zoom);
        double centerWorldY = latToWorldY(centerLat, zoom);
        return new double[]{
                canvas.getWidth() / 2 + (worldX - centerWorldX),
                canvas.getHeight() / 2 + (worldY - centerWorldY)
        };
    }

    /**
     * Function to instantly jump the camera to fit a set of points, with no animation. Used only
     * for the deferred fit that runs once the canvas first gets a real size, since at that point
     * nothing has been drawn yet for an animation to glide away from.
     *
     * @param points the points to fit, in (lat, lng) pairs
     */
    void fitToPointsInstant(List<double[]> points) {
        MapCameraFit.CameraTarget target = MapCameraFit.compute(points, canvas.getWidth(), canvas.getHeight());
        if (target == null) {
            return;
        }
        centerLat = target.lat();
        centerLng = target.lng();
        zoom = target.zoom();
    }

    /**
     * Function to glide the camera to fit a set of points. The caller is responsible for
     * deferring this call until the canvas has a real size.
     *
     * @param points the points to fit, in (lat, lng) pairs
     */
    void fitToPointsAnimated(List<double[]> points) {
        MapCameraFit.CameraTarget target = MapCameraFit.compute(points, canvas.getWidth(), canvas.getHeight());
        if (target != null) {
            animateTo(target.lat(), target.lng(), target.zoom());
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
