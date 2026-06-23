package com.strazzullo_marocco_sibilla_marin.app.geo;

/**
 * A geographic coordinate pair resolved from an address by a {@link GeocodingService}.
 *
 * @param latitude the latitude coordinate
 * @param longitude the longitude coordinate
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public record GeoPoint(double latitude, double longitude) {
}
