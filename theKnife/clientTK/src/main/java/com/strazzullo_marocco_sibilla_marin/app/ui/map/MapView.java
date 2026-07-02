package com.strazzullo_marocco_sibilla_marin.app.ui.map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interactive map panel rendered natively on a JavaFX {@link Canvas}, with no embedded browser
 * engine: Web Mercator tile math, camera animation, pin markers/popup, and the floating zoom/
 * locate/recenter buttons each live in their own class ({@link MapCamera}, {@link MapTileLoader},
 * {@link MapPinOverlay}, {@link MapControls}); this class wires them together and owns the
 * canvas repaint loop and the "you are here"/search-radius markers.
 *
 * @version 4.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class MapView extends StackPane {

    private final Canvas canvas = new Canvas();
    private final Pane overlay = new Pane();
    private final Pane mapLayer = new Pane();
    private final Scale zoomScale = new Scale(1, 1, 0, 0);
    private Runnable onLocateRequest;

    private final MapCamera camera = new MapCamera(canvas, zoomScale, this::redraw);
    private final MapTileLoader tileLoader = new MapTileLoader();
    private final MapPinOverlay pinOverlay = new MapPinOverlay(overlay, camera::projectToScreen, this::onPinsChanged);
    private final MapMarkerLayer markers = new MapMarkerLayer(overlay, camera::projectToScreen);
    private final MapControls controls = new MapControls(
            () -> camera.zoomAtScreenPoint(canvas.getWidth() / 2, canvas.getHeight() / 2, 1),
            () -> camera.zoomAtScreenPoint(canvas.getWidth() / 2, canvas.getHeight() / 2, -1),
            () -> { if (onLocateRequest != null) onLocateRequest.run(); },
            this::recenterOnContent);

    private List<double[]> pendingFitPoints;

    /**
     * MapView constructor. Builds the tile canvas, the marker overlay, and the pan/zoom handlers.
     */
    public MapView() {
        overlay.setPickOnBounds(false);

        mapLayer.getChildren().addAll(canvas, overlay);
        mapLayer.getTransforms().add(zoomScale);

        StackPane.setAlignment(controls, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(controls, new Insets(16));

        getChildren().addAll(mapLayer, controls);

        widthProperty().addListener((obs, oldW, newW) -> onSizeChanged());
        heightProperty().addListener((obs, oldH, newH) -> onSizeChanged());

        new MapInputHandler(canvas, camera, pinOverlay);

        Rectangle clip = new Rectangle();
        clip.setArcWidth(32);
        clip.setArcHeight(32);
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
    }

    /**
     * Function to register a callback invoked when the user presses the locate-me button, used
     * by the host screen to prompt for (or auto-detect) the user's position.
     *
     * @param handler the callback to invoke
     */
    public void setOnLocateRequest(Runnable handler) {
        this.onLocateRequest = handler;
    }

    /**
     * Function to show or hide the floating zoom/locate/recenter controls, used to turn this
     * into a quiet, read-only map preview (e.g. a single static pin on the location detail
     * screen) without losing mouse pan/scroll-zoom, which aren't tied to the buttons.
     *
     * @param visible whether the controls should be shown
     */
    public void setControlsVisible(boolean visible) {
        controls.setVisible(visible);
        controls.setManaged(visible);
        pinOverlay.setPopupsEnabled(visible);
    }

    /**
     * Function to wire up the pin popup's "Dettagli" button, called with the clicked pin's id
     * (the location id) when pressed.
     *
     * @param handler the callback to invoke with the pin id, or null to clear it
     */
    public void setOnPinDetails(Consumer<String> handler) {
        pinOverlay.setOnPinDetails(handler);
    }

    /**
     * Function to refit the view to whatever is currently shown (pins, the search radius circle,
     * or the user's own position), falling back to the default Italy-wide view if nothing is
     * displayed.
     */
    private void recenterOnContent() {
        List<double[]> points = new ArrayList<>();
        pinOverlay.pins().forEach(pin -> points.add(new double[]{pin.lat(), pin.lng()}));
        points.addAll(markers.radiusBoundsPoints());
        points.addAll(markers.userLocationPoint());
        if (points.isEmpty()) {
            camera.animateTo(MapCamera.ITALY_LAT, MapCamera.ITALY_LNG, MapCamera.ITALY_ZOOM);
        } else {
            fitToPointsAnimated(points);
        }
    }

    /**
     * Function to merge a newly-set pin list's coordinates with the search radius circle's bounds
     * (if shown) and refit the camera to all of it, called by {@link #pinOverlay} whenever its
     * pin set changes.
     *
     * @param pinPoints the current pins' coordinates, in (lat, lng) pairs
     */
    private void onPinsChanged(List<double[]> pinPoints) {
        List<double[]> points = new ArrayList<>(pinPoints);
        points.addAll(markers.radiusBoundsPoints());
        fitToPointsAnimated(points);
    }

    /**
     * Function to resize the canvas to match this view's current size, and, once a real size is
     * finally available, resolve any pending initial pin-fit.
     */
    private void onSizeChanged() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        if (pendingFitPoints != null && getWidth() > 0 && getHeight() > 0) {
            List<double[]> points = pendingFitPoints;
            pendingFitPoints = null;
            camera.fitToPointsInstant(points);
        }
        redraw();
    }

    /**
     * Function to repaint the tile layer and reposition every overlay node for the current
     * center/zoom. Missing tiles are requested asynchronously and trigger another repaint once
     * they arrive.
     */
    private void redraw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        int zoom = camera.zoom();
        MapTileRenderer.paint(canvas.getGraphicsContext2D(), w, h, zoom, camera.centerWorld(), tileLoader, this::redraw);

        pinOverlay.reposition();
        markers.reposition(zoom);
    }

    /**
     * Function to replace every marker currently shown on the map, fitting the view to them.
     *
     * @param newPins the pins to display
     */
    public void setPins(List<MapPin> newPins) {
        pinOverlay.setPins(newPins);
    }

    /**
     * Function to center the map on a single pin, zoom in on it, and open its popup.
     *
     * @param pinId the id of the pin to focus
     */
    public void focusPin(String pinId) {
        MapPin pin = pinOverlay.get(pinId);
        if (pin == null) {
            return;
        }
        camera.animateTo(pin.lat(), pin.lng(), Math.max(camera.zoom(), 14));
        pinOverlay.focusPin(pinId);
    }

    /**
     * Function to show (or move) the "you are here" marker for the distance filter's
     * reference point.
     *
     * @param lat the reference point latitude
     * @param lng the reference point longitude
     */
    public void setUserLocation(double lat, double lng) {
        markers.setUserLocation(lat, lng);
        redraw();
    }

    /**
     * Function to remove the "you are here" marker, if shown.
     */
    public void clearUserLocation() {
        markers.clearUserLocation();
    }

    /**
     * Function to draw (or move/resize) the search radius circle around a reference point,
     * fitting the view to it.
     *
     * @param lat the reference point latitude
     * @param lng the reference point longitude
     * @param radiusKm the search radius in kilometers
     */
    public void setSearchRadius(double lat, double lng, double radiusKm) {
        markers.setSearchRadius(lat, lng, radiusKm);
        fitToPointsAnimated(markers.radiusBoundsPoints());
    }

    /**
     * Function to remove the search radius circle, if shown.
     */
    public void clearSearchRadius() {
        markers.clearSearchRadius();
    }

    /**
     * Function to glide the camera to fit a set of points, deferring to {@link #pendingFitPoints}
     * if the canvas doesn't have a real size yet (in which case there's nothing on screen yet to
     * animate away from).
     *
     * @param points the points to fit, in (lat, lng) pairs
     */
    private void fitToPointsAnimated(List<double[]> points) {
        if (points.isEmpty()) {
            redraw();
            return;
        }
        if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
            pendingFitPoints = points;
            return;
        }
        camera.fitToPointsAnimated(points);
    }
}
