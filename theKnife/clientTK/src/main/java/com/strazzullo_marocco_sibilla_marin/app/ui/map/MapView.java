package com.strazzullo_marocco_sibilla_marin.app.ui.map;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interactive map panel rendered natively on a JavaFX {@link Canvas}: Web Mercator tile math,
 * asynchronous tile fetches via {@link HttpClient}, and vector overlay nodes for markers. There
 * is no embedded browser engine involved, which is what the previous WebView/Leaflet
 * implementation relied on: that engine's old WebKit could not keep Leaflet's CSS-transform-based
 * pan/zoom tiles aligned, leaving visibly misplaced tile fragments behind during interaction.
 * Tiles come from the free, key-less CARTO Voyager basemap (OpenStreetMap data).
 *
 * @version 3.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class MapView extends StackPane {

    private static final Logger LOGGER = Logger.getLogger(MapView.class.getName());

    private static final int TILE_SIZE = 256;
    private static final int MIN_ZOOM = 3;
    private static final int MAX_ZOOM = 18;
    private static final int FIT_MAX_ZOOM = 15;
    private static final double FIT_PADDING_PX = 40;

    private static final double ITALY_LAT = 41.9;
    private static final double ITALY_LNG = 12.5;
    private static final int ITALY_ZOOM = 6;

    private static final String[] TILE_SUBDOMAINS = {"a", "b", "c", "d"};
    private static final String TILE_URL_TEMPLATE =
            "https://%s.basemaps.cartocdn.com/rastertiles/voyager/%d/%d/%d.png";

    /**
     * Trackpads and mice deliver one scroll event per "notch"/frame; a single event always zooms
     * by one level, but events arriving faster than this cooldown (e.g. trackpad momentum) are
     * dropped so a long scroll gesture doesn't fly through dozens of zoom levels at once.
     */
    private static final long SCROLL_ZOOM_COOLDOWN_NANOS = 90_000_000L;

    /**
     * Duration of the eased transition used whenever the camera (center/zoom) changes
     * programmatically (search results, "locate me", focusing a pin, zoom buttons), so the map
     * glides there instead of jump-cutting. Direct mouse drag/scroll stay real-time/un-animated,
     * since they're already driven frame-by-frame by the user's own input.
     */
    private static final double CAMERA_ANIMATION_MILLIS = 320;

    private final Canvas canvas = new Canvas();
    private final Pane overlay = new Pane();
    private final Pane mapLayer = new Pane();
    private final Label popupLabel = new Label();

    /**
     * Applied to {@link #mapLayer} to fake a continuous zoom while a {@link #cameraAnimation} is
     * in flight: tiles are drawn once at the animation's starting integer zoom level and this
     * transform scales them smoothly towards the target level's apparent size, instead of
     * re-fetching/redrawing tiles every frame. Reset to identity once the animation lands, at
     * which point a normal {@link #redraw()} fetches the target zoom's real tiles.
     */
    private final Scale zoomScale = new Scale(1, 1, 0, 0);

    private Animation cameraAnimation;
    private double animatingToLat;
    private double animatingToLng;
    private int animatingToZoom;

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Map<String, Image> tileCache = new ConcurrentHashMap<>();
    private final Set<String> tilesInFlight = ConcurrentHashMap.newKeySet();

    private final Map<String, MapPin> pins = new HashMap<>();
    private final Map<String, Circle> pinNodes = new HashMap<>();

    private double centerLat = ITALY_LAT;
    private double centerLng = ITALY_LNG;
    private int zoom = ITALY_ZOOM;

    private String focusedPinId;
    private List<double[]> pendingFitPoints;

    private Circle userMarker;
    private double userLat;
    private double userLng;

    private Circle radiusCircle;
    private double radiusLat;
    private double radiusLng;
    private double radiusKm = -1;

    private double dragAnchorScreenX;
    private double dragAnchorScreenY;
    private double dragAnchorCenterWorldX;
    private double dragAnchorCenterWorldY;

    private long lastScrollZoomNanos;
    private Runnable onLocateRequest;
    private final VBox controls;

    /**
     * MapView constructor. Builds the tile canvas, the marker overlay, and the pan/zoom handlers.
     */
    public MapView() {
        overlay.setPickOnBounds(false);
        popupLabel.getStyleClass().add("tk-map-popup");
        popupLabel.setVisible(false);
        popupLabel.setMouseTransparent(true);
        overlay.getChildren().add(popupLabel);

        mapLayer.getChildren().addAll(canvas, overlay);
        mapLayer.getTransforms().add(zoomScale);

        controls = buildControls();
        StackPane.setAlignment(controls, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(controls, new Insets(16));

        getChildren().addAll(mapLayer, controls);

        widthProperty().addListener((obs, oldW, newW) -> onSizeChanged());
        heightProperty().addListener((obs, oldH, newH) -> onSizeChanged());

        canvas.setOnScroll(this::handleScroll);
        canvas.setOnMousePressed(event -> {
            cancelCameraAnimation();
            dragAnchorScreenX = event.getX();
            dragAnchorScreenY = event.getY();
            dragAnchorCenterWorldX = lonToWorldX(centerLng, zoom);
            dragAnchorCenterWorldY = latToWorldY(centerLat, zoom);
        });
        canvas.setOnMouseDragged(event -> {
            double newCenterWorldX = dragAnchorCenterWorldX - (event.getX() - dragAnchorScreenX);
            double newCenterWorldY = dragAnchorCenterWorldY - (event.getY() - dragAnchorScreenY);
            centerLng = worldXToLon(newCenterWorldX, zoom);
            centerLat = worldYToLat(newCenterWorldY, zoom);
            redraw();
        });
        canvas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                zoomAtScreenPoint(event.getX(), event.getY(), 1);
            } else if (popupLabel.isVisible()) {
                popupLabel.setVisible(false);
                focusedPinId = null;
                refreshPinStyles();
            }
        });

        Rectangle clip = new Rectangle();
        clip.setArcWidth(32);
        clip.setArcHeight(32);
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
    }

    /**
     * Function to build the floating zoom in/out, locate-me and recenter buttons, stacked in the
     * corner of the map.
     *
     * @return the controls box
     */
    private VBox buildControls() {
        Button zoomIn = mapButton(Feather.PLUS, "tk-map-control-button");
        zoomIn.setOnAction(e -> zoomAtScreenPoint(canvas.getWidth() / 2, canvas.getHeight() / 2, 1));

        Button zoomOut = mapButton(Feather.MINUS, "tk-map-control-button");
        zoomOut.setOnAction(e -> zoomAtScreenPoint(canvas.getWidth() / 2, canvas.getHeight() / 2, -1));

        VBox zoomGroup = new VBox(zoomIn, new Separator(), zoomOut);
        zoomGroup.getStyleClass().add("tk-map-zoom-group");
        zoomGroup.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        zoomGroup.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        Button locate = mapButton(Feather.NAVIGATION, "tk-map-locate-button");
        locate.setOnAction(e -> {
            if (onLocateRequest != null) {
                onLocateRequest.run();
            }
        });

        Button recenter = mapButton(Feather.MAXIMIZE, "tk-map-recenter-button");
        recenter.setOnAction(e -> recenterOnContent());

        VBox controls = new VBox(10, zoomGroup, locate, recenter);
        controls.setAlignment(Pos.CENTER);
        controls.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        controls.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        return controls;
    }

    /**
     * Function to build a single floating, square map control button with an explicitly colored
     * icon, so its visibility doesn't depend on inherited text-fill from the surrounding theme.
     *
     * @param icon the icon to display
     * @param styleClass the CSS class providing the button's background/shape
     * @return the button
     */
    private Button mapButton(Feather icon, String styleClass) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.getStyleClass().add("tk-map-control-icon");
        Button button = new Button("", fontIcon);
        button.getStyleClass().add(styleClass);
        return button;
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
    }

    /**
     * Function to refit the view to whatever is currently shown (pins, the search radius circle,
     * or the user's own position), falling back to the default Italy-wide view if nothing is
     * displayed.
     */
    private void recenterOnContent() {
        List<double[]> points = new ArrayList<>();
        pins.values().forEach(pin -> points.add(new double[]{pin.lat(), pin.lng()}));
        if (radiusCircle != null) {
            addCircleBoundsTo(points, radiusLat, radiusLng, radiusKm);
        }
        if (userMarker != null) {
            points.add(new double[]{userLat, userLng});
        }
        if (points.isEmpty()) {
            animateCamera(ITALY_LAT, ITALY_LNG, ITALY_ZOOM);
        } else {
            fitToPointsAnimated(points);
        }
    }

    private void onSizeChanged() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        if (pendingFitPoints != null && getWidth() > 0 && getHeight() > 0) {
            List<double[]> points = pendingFitPoints;
            pendingFitPoints = null;
            fitToPointsInstant(points);
        }
        redraw();
    }

    private void handleScroll(ScrollEvent event) {
        event.consume();
        if (event.isInertia() || event.getDeltaY() == 0) {
            return;
        }
        long now = System.nanoTime();
        if (now - lastScrollZoomNanos < SCROLL_ZOOM_COOLDOWN_NANOS) {
            return;
        }
        lastScrollZoomNanos = now;
        zoomAtScreenPoint(event.getX(), event.getY(), event.getDeltaY() > 0 ? 1 : -1);
    }

    private void zoomAtScreenPoint(double screenX, double screenY, int direction) {
        int newZoom = clamp(zoom + direction, MIN_ZOOM, MAX_ZOOM);
        if (newZoom == zoom) {
            return;
        }
        double[] underCursor = screenToLatLng(screenX, screenY);
        double targetWorldX = lonToWorldX(underCursor[1], newZoom);
        double targetWorldY = latToWorldY(underCursor[0], newZoom);
        double newCenterWorldX = targetWorldX - (screenX - canvas.getWidth() / 2);
        double newCenterWorldY = targetWorldY - (screenY - canvas.getHeight() / 2);
        animateCamera(worldYToLat(newCenterWorldY, newZoom), worldXToLon(newCenterWorldX, newZoom), newZoom);
    }

    /**
     * Function to glide the camera (center and zoom) to a new state instead of jump-cutting,
     * used by every programmatic camera change (search recenter, focusing a pin, zoom controls).
     * Tiles are drawn once at the animation's starting zoom level and {@link #zoomScale} fakes
     * the rest of the zoom transition, so no extra tile fetches happen mid-animation; the target
     * zoom's real tiles are only fetched once the camera lands.
     *
     * @param targetLat the latitude to end up centered on
     * @param targetLng the longitude to end up centered on
     * @param targetZoom the zoom level to end up at
     */
    private void animateCamera(double targetLat, double targetLng, int targetZoom) {
        cancelCameraAnimation();
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
                setCycleDuration(javafx.util.Duration.millis(CAMERA_ANIMATION_MILLIS));
            }

            @Override
            protected void interpolate(double frac) {
                centerLat = startLat + (targetLat - startLat) * frac;
                centerLng = startLng + (targetLng - startLng) * frac;
                double scale = 1 + (targetScale - 1) * frac;
                zoomScale.setX(scale);
                zoomScale.setY(scale);
                redraw();
            }
        };
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.setOnFinished(e -> finalizeCameraTo(targetLat, targetLng, targetZoom));

        animatingToLat = targetLat;
        animatingToLng = targetLng;
        animatingToZoom = targetZoom;
        cameraAnimation = transition;
        transition.play();
    }

    /**
     * Function to immediately settle any in-flight {@link #cameraAnimation} at the state it was
     * animating towards, used before starting a new camera animation and before a user drag
     * takes over, so neither ever has to deal with a mid-animation {@link #zoomScale}.
     */
    private void cancelCameraAnimation() {
        if (cameraAnimation == null) {
            return;
        }
        cameraAnimation.stop();
        finalizeCameraTo(animatingToLat, animatingToLng, animatingToZoom);
    }

    private void finalizeCameraTo(double lat, double lng, int targetZoom) {
        centerLat = lat;
        centerLng = lng;
        zoom = targetZoom;
        zoomScale.setX(1);
        zoomScale.setY(1);
        cameraAnimation = null;
        redraw();
    }

    private double[] screenToLatLng(double screenX, double screenY) {
        double worldX = lonToWorldX(centerLng, zoom) + (screenX - canvas.getWidth() / 2);
        double worldY = latToWorldY(centerLat, zoom) + (screenY - canvas.getHeight() / 2);
        return new double[]{worldYToLat(worldY, zoom), worldXToLon(worldX, zoom)};
    }

    private double[] projectToScreen(double lat, double lng) {
        double worldX = lonToWorldX(lng, zoom);
        double worldY = latToWorldY(lat, zoom);
        double centerWorldX = lonToWorldX(centerLng, zoom);
        double centerWorldY = latToWorldY(centerLat, zoom);
        return new double[]{
                canvas.getWidth() / 2 + (worldX - centerWorldX),
                canvas.getHeight() / 2 + (worldY - centerWorldY)
        };
    }

    private static double lonToWorldX(double lon, int zoom) {
        return (lon + 180.0) / 360.0 * tilesPerSide(zoom) * TILE_SIZE;
    }

    private static double latToWorldY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        double y = (1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2;
        return y * tilesPerSide(zoom) * TILE_SIZE;
    }

    private static double worldXToLon(double worldX, int zoom) {
        return worldX / (tilesPerSide(zoom) * TILE_SIZE) * 360.0 - 180.0;
    }

    private static double worldYToLat(double worldY, int zoom) {
        double y = worldY / (tilesPerSide(zoom) * TILE_SIZE);
        double latRad = Math.atan(Math.sinh(Math.PI * (1 - 2 * y)));
        return Math.toDegrees(latRad);
    }

    private static double tilesPerSide(int zoom) {
        return 1 << zoom;
    }

    private static double metersPerPixel(double lat, int zoom) {
        return 156543.03392 * Math.cos(Math.toRadians(lat)) / tilesPerSide(zoom);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
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
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#dfe3e6"));
        gc.fillRect(0, 0, w, h);

        int n = (int) tilesPerSide(zoom);
        double centerWorldX = lonToWorldX(centerLng, zoom);
        double centerWorldY = latToWorldY(centerLat, zoom);
        double topLeftWorldX = centerWorldX - w / 2;
        double topLeftWorldY = centerWorldY - h / 2;

        int firstTileX = (int) Math.floor(topLeftWorldX / TILE_SIZE);
        int lastTileX = (int) Math.floor((topLeftWorldX + w) / TILE_SIZE);
        int firstTileY = (int) Math.floor(topLeftWorldY / TILE_SIZE);
        int lastTileY = (int) Math.floor((topLeftWorldY + h) / TILE_SIZE);

        for (int tx = firstTileX; tx <= lastTileX; tx++) {
            for (int ty = firstTileY; ty <= lastTileY; ty++) {
                if (ty < 0 || ty >= n) {
                    continue;
                }
                int wrappedX = ((tx % n) + n) % n;
                String key = zoom + "/" + wrappedX + "/" + ty;
                double screenX = tx * (double) TILE_SIZE - topLeftWorldX;
                double screenY = ty * (double) TILE_SIZE - topLeftWorldY;
                Image tile = tileCache.get(key);
                if (tile != null) {
                    gc.drawImage(tile, screenX, screenY, TILE_SIZE, TILE_SIZE);
                } else {
                    requestTile(zoom, wrappedX, ty, key);
                }
            }
        }

        positionOverlays();
    }

    private void requestTile(int z, int wrappedX, int y, String key) {
        if (!tilesInFlight.add(key)) {
            return;
        }
        String subdomain = TILE_SUBDOMAINS[(wrappedX + y) % TILE_SUBDOMAINS.length];
        String url = String.format(TILE_URL_TEMPLATE, subdomain, z, wrappedX, y);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).GET().build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        tileCache.put(key, new Image(new ByteArrayInputStream(response.body())));
                        Platform.runLater(this::redraw);
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

    private void positionOverlays() {
        for (Map.Entry<String, MapPin> entry : pins.entrySet()) {
            Circle node = pinNodes.get(entry.getKey());
            if (node == null) {
                continue;
            }
            double[] screen = projectToScreen(entry.getValue().lat(), entry.getValue().lng());
            node.setCenterX(screen[0]);
            node.setCenterY(screen[1]);
        }
        if (userMarker != null) {
            double[] screen = projectToScreen(userLat, userLng);
            userMarker.setCenterX(screen[0]);
            userMarker.setCenterY(screen[1]);
        }
        if (radiusCircle != null) {
            double[] screen = projectToScreen(radiusLat, radiusLng);
            radiusCircle.setCenterX(screen[0]);
            radiusCircle.setCenterY(screen[1]);
            radiusCircle.setRadius(radiusKm * 1000 / metersPerPixel(radiusLat, zoom));
        }
        if (focusedPinId != null && popupLabel.isVisible()) {
            positionPopup(focusedPinId);
        }
    }

    private void positionPopup(String pinId) {
        MapPin pin = pins.get(pinId);
        if (pin == null) {
            return;
        }
        double[] screen = projectToScreen(pin.lat(), pin.lng());
        Platform.runLater(() -> {
            popupLabel.setLayoutX(screen[0] - popupLabel.getWidth() / 2);
            popupLabel.setLayoutY(screen[1] - popupLabel.getHeight() - 18);
        });
    }

    private void showPopupFor(String pinId) {
        MapPin pin = pins.get(pinId);
        if (pin == null) {
            return;
        }
        focusedPinId = pinId;
        popupLabel.setText(pin.title() + "\n" + pin.subtitle());
        popupLabel.setVisible(true);
        popupLabel.toFront();
        refreshPinStyles();
        positionPopup(pinId);
    }

    private void refreshPinStyles() {
        pinNodes.forEach((id, node) -> {
            node.getStyleClass().setAll(id.equals(focusedPinId) ? "tk-map-pin-focused" : "tk-map-pin");
        });
    }

    /**
     * Function to replace every marker currently shown on the map, fitting the view to them.
     *
     * @param newPins the pins to display
     */
    public void setPins(List<MapPin> newPins) {
        overlay.getChildren().removeAll(pinNodes.values());
        pinNodes.clear();
        pins.clear();
        focusedPinId = null;
        popupLabel.setVisible(false);

        List<double[]> points = new ArrayList<>();
        for (MapPin pin : newPins) {
            pins.put(pin.id(), pin);
            points.add(new double[]{pin.lat(), pin.lng()});

            Circle node = new Circle(7);
            node.getStyleClass().add("tk-map-pin");
            node.setOnMouseClicked(event -> {
                if (pin.id().equals(focusedPinId) && popupLabel.isVisible()) {
                    popupLabel.setVisible(false);
                    focusedPinId = null;
                    refreshPinStyles();
                } else {
                    showPopupFor(pin.id());
                }
                event.consume();
            });
            pinNodes.put(pin.id(), node);
            overlay.getChildren().add(node);
        }
        if (radiusCircle != null) {
            addCircleBoundsTo(points, radiusLat, radiusLng, radiusKm);
        }
        fitToPointsAnimated(points);
    }

    /**
     * Function to center the map on a single pin, zoom in on it, and open its popup.
     *
     * @param pinId the id of the pin to focus
     */
    public void focusPin(String pinId) {
        MapPin pin = pins.get(pinId);
        if (pin == null) {
            return;
        }
        animateCamera(pin.lat(), pin.lng(), Math.max(zoom, 14));
        showPopupFor(pinId);
    }

    /**
     * Function to show (or move) the "you are here" marker for the distance filter's
     * reference point.
     *
     * @param lat the reference point latitude
     * @param lng the reference point longitude
     */
    public void setUserLocation(double lat, double lng) {
        userLat = lat;
        userLng = lng;
        if (userMarker == null) {
            userMarker = new Circle(7);
            userMarker.getStyleClass().add("tk-map-user-pin");
            userMarker.setMouseTransparent(true);
            overlay.getChildren().add(userMarker);
        }
        redraw();
    }

    /**
     * Function to remove the "you are here" marker, if shown.
     */
    public void clearUserLocation() {
        if (userMarker != null) {
            overlay.getChildren().remove(userMarker);
            userMarker = null;
        }
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
        this.radiusLat = lat;
        this.radiusLng = lng;
        this.radiusKm = radiusKm;
        if (radiusCircle == null) {
            radiusCircle = new Circle();
            radiusCircle.getStyleClass().add("tk-map-radius-circle");
            radiusCircle.setMouseTransparent(true);
            overlay.getChildren().add(0, radiusCircle);
        }
        List<double[]> points = new ArrayList<>();
        addCircleBoundsTo(points, lat, lng, radiusKm);
        fitToPointsAnimated(points);
    }

    /**
     * Function to remove the search radius circle, if shown.
     */
    public void clearSearchRadius() {
        if (radiusCircle != null) {
            overlay.getChildren().remove(radiusCircle);
            radiusCircle = null;
        }
        radiusKm = -1;
    }

    private static void addCircleBoundsTo(List<double[]> points, double lat, double lng, double radiusKm) {
        double latRadiusDeg = radiusKm / 111.0;
        double lngRadiusDeg = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));
        points.add(new double[]{lat + latRadiusDeg, lng + lngRadiusDeg});
        points.add(new double[]{lat - latRadiusDeg, lng - lngRadiusDeg});
    }

    /**
     * Function to instantly jump the camera to fit a set of points, with no animation. Used only
     * for the deferred fit that runs once the canvas first gets a real size (see {@link
     * #pendingFitPoints}), since at that point nothing has been drawn yet for an animation to
     * glide away from.
     *
     * @param points the points to fit, in (lat, lng) pairs
     */
    private void fitToPointsInstant(List<double[]> points) {
        CameraTarget target = computeFit(points);
        if (target == null) {
            return;
        }
        centerLat = target.lat();
        centerLng = target.lng();
        zoom = target.zoom();
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
        CameraTarget target = computeFit(points);
        animateCamera(target.lat(), target.lng(), target.zoom());
    }

    /**
     * @param lat the latitude to center on
     * @param lng the longitude to center on
     * @param zoom the zoom level fitting every point with some padding
     */
    private record CameraTarget(double lat, double lng, int zoom) {
    }

    /**
     * Function to compute the camera state that fits a set of points on screen with some padding,
     * picking the highest zoom level at which they all still fit.
     *
     * @param points the points to fit, in (lat, lng) pairs
     * @return the fitting camera state, or null if the canvas has no real size yet or there are
     *         no points to fit
     */
    private CameraTarget computeFit(List<double[]> points) {
        if (points.isEmpty()) {
            return null;
        }
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) {
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
            if (spanX <= w - 2 * FIT_PADDING_PX && spanY <= h - 2 * FIT_PADDING_PX) {
                fitZoom = z;
                break;
            }
        }
        return new CameraTarget(fitLat, fitLng, fitZoom);
    }
}
