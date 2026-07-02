package com.strazzullo_marocco_sibilla_marin.app.ui.map;

/**
 * A single marker to display on the {@link MapView}.
 *
 * @param id the location id the pin represents
 * @param lat the marker latitude
 * @param lng the marker longitude
 * @param title the marker popup title, typically the restaurant name
 * @param subtitle the marker popup subtitle, typically the address
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 * @version 1.0
 */
public record MapPin(String id, double lat, double lng, String title, String subtitle) {
}
