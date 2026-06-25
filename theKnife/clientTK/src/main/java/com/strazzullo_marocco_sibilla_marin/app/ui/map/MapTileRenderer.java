package com.strazzullo_marocco_sibilla_marin.app.ui.map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static com.strazzullo_marocco_sibilla_marin.app.ui.map.WebMercatorProjection.TILE_SIZE;
import static com.strazzullo_marocco_sibilla_marin.app.ui.map.WebMercatorProjection.tilesPerSide;

/**
 * Paints the visible raster tiles for the current camera state onto {@link MapView}'s canvas,
 * requesting any tile not yet in {@link MapTileLoader}'s cache.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
final class MapTileRenderer {

    private MapTileRenderer() {
    }

    /**
     * @param gc the canvas graphics context to paint onto
     * @param width the canvas width
     * @param height the canvas height
     * @param zoom the current zoom level
     * @param centerWorld the current camera center, in world pixel coordinates at this zoom
     * @param tileLoader the tile cache/fetcher to read from and request missing tiles from
     * @param onTileLoaded called (on the JavaFX application thread) once a requested tile arrives
     */
    static void paint(GraphicsContext gc, double width, double height, int zoom, double[] centerWorld,
                       MapTileLoader tileLoader, Runnable onTileLoaded) {
        gc.setFill(Color.web("#dfe3e6"));
        gc.fillRect(0, 0, width, height);

        int n = (int) tilesPerSide(zoom);
        double topLeftWorldX = centerWorld[0] - width / 2;
        double topLeftWorldY = centerWorld[1] - height / 2;

        int firstTileX = (int) Math.floor(topLeftWorldX / TILE_SIZE);
        int lastTileX = (int) Math.floor((topLeftWorldX + width) / TILE_SIZE);
        int firstTileY = (int) Math.floor(topLeftWorldY / TILE_SIZE);
        int lastTileY = (int) Math.floor((topLeftWorldY + height) / TILE_SIZE);

        for (int tx = firstTileX; tx <= lastTileX; tx++) {
            for (int ty = firstTileY; ty <= lastTileY; ty++) {
                if (ty < 0 || ty >= n) {
                    continue;
                }
                int wrappedX = ((tx % n) + n) % n;
                double screenX = tx * (double) TILE_SIZE - topLeftWorldX;
                double screenY = ty * (double) TILE_SIZE - topLeftWorldY;
                Image tile = tileLoader.get(zoom, wrappedX, ty);
                if (tile != null) {
                    gc.drawImage(tile, screenX, screenY, TILE_SIZE, TILE_SIZE);
                } else {
                    tileLoader.request(zoom, wrappedX, ty, onTileLoaded);
                }
            }
        }
    }
}
