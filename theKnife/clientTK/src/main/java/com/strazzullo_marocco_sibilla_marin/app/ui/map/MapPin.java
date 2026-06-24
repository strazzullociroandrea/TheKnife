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
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Marin Marco, 760622, VA
 * @author Sibilla Ginevra, 76114
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @version 1.0
 */
public record MapPin(String id, double lat, double lng, String title, String subtitle) {
}
