package com.strazzullo_marocco_sibilla_marin.app.ui.map;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.ScrollEvent;

/**
 * Wires up {@link MapView}'s mouse/scroll interaction onto its canvas: drag-to-pan, scroll and
 * double-click zoom, and dismissing the pin popup on a plain click. Pure event plumbing, kept out
 * of {@link MapView} itself.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
final class MapInputHandler {

    /**
     * Trackpads and mice deliver one scroll event per "notch"/frame; a single event always zooms
     * by one level, but events arriving faster than this cooldown (e.g. trackpad momentum) are
     * dropped so a long scroll gesture doesn't fly through dozens of zoom levels at once.
     */
    private static final long SCROLL_ZOOM_COOLDOWN_NANOS = 90_000_000L;

    private double dragAnchorScreenX;
    private double dragAnchorScreenY;
    private double dragAnchorCenterWorldX;
    private double dragAnchorCenterWorldY;
    private long lastScrollZoomNanos;

    /**
     * @param canvas the canvas to attach handlers to
     * @param camera the camera panned/zoomed by user interaction
     * @param pinOverlay the pin overlay whose popup a plain click should dismiss
     */
    MapInputHandler(Canvas canvas, MapCamera camera, MapPinOverlay pinOverlay) {
        canvas.setOnScroll(event -> handleScroll(event, camera));
        canvas.setOnMousePressed(event -> {
            camera.cancelAnimation();
            dragAnchorScreenX = event.getX();
            dragAnchorScreenY = event.getY();
            double[] world = camera.centerWorld();
            dragAnchorCenterWorldX = world[0];
            dragAnchorCenterWorldY = world[1];
        });
        canvas.setOnMouseDragged(event -> {
            double newCenterWorldX = dragAnchorCenterWorldX - (event.getX() - dragAnchorScreenX);
            double newCenterWorldY = dragAnchorCenterWorldY - (event.getY() - dragAnchorScreenY);
            camera.panToWorld(newCenterWorldX, newCenterWorldY);
        });
        canvas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                camera.zoomAtScreenPoint(event.getX(), event.getY(), 1);
            } else if (pinOverlay.isPopupVisible()) {
                pinOverlay.hidePopup();
            }
        });
    }

    private void handleScroll(ScrollEvent event, MapCamera camera) {
        event.consume();
        if (event.isInertia() || event.getDeltaY() == 0) {
            return;
        }
        long now = System.nanoTime();
        if (now - lastScrollZoomNanos < SCROLL_ZOOM_COOLDOWN_NANOS) {
            return;
        }
        lastScrollZoomNanos = now;
        camera.zoomAtScreenPoint(event.getX(), event.getY(), event.getDeltaY() > 0 ? 1 : -1);
    }
}
