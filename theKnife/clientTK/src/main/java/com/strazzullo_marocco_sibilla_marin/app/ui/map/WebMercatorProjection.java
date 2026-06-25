package com.strazzullo_marocco_sibilla_marin.app.ui.map;

/**
 * Pure Web Mercator tile math shared by {@link MapView}: converting between geographic
 * coordinates (latitude/longitude) and the "world pixel" space tiles are addressed in at a given
 * zoom level, plus the distance-to-pixel conversion the search radius circle needs.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
final class WebMercatorProjection {

    static final int TILE_SIZE = 256;

    private WebMercatorProjection() {
    }

    static double lonToWorldX(double lon, int zoom) {
        return (lon + 180.0) / 360.0 * tilesPerSide(zoom) * TILE_SIZE;
    }

    static double latToWorldY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        double y = (1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2;
        return y * tilesPerSide(zoom) * TILE_SIZE;
    }

    static double worldXToLon(double worldX, int zoom) {
        return worldX / (tilesPerSide(zoom) * TILE_SIZE) * 360.0 - 180.0;
    }

    static double worldYToLat(double worldY, int zoom) {
        double y = worldY / (tilesPerSide(zoom) * TILE_SIZE);
        double latRad = Math.atan(Math.sinh(Math.PI * (1 - 2 * y)));
        return Math.toDegrees(latRad);
    }

    static double tilesPerSide(int zoom) {
        return 1 << zoom;
    }

    static double metersPerPixel(double lat, int zoom) {
        return 156543.03392 * Math.cos(Math.toRadians(lat)) / tilesPerSide(zoom);
    }
}
