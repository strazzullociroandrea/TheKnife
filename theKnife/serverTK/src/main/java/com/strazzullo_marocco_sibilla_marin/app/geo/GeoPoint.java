package com.strazzullo_marocco_sibilla_marin.app.geo;

/**
 * A geographic coordinate pair resolved from an address by a {@link GeocodingService}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class GeoPoint {

    private final double latitude;
    private final double longitude;

    /**
     * GeoPoint constructor.
     *
     * @param latitude  the latitude coordinate
     * @param longitude the longitude coordinate
     */
    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * getLatitude
     *
     * @return returns latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * getLongitude
     *
     * @return returns longitude
     */
    public double getLongitude() {
        return longitude;
    }
}
