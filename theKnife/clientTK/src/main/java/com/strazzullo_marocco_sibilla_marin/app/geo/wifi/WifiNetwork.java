package com.strazzullo_marocco_sibilla_marin.app.geo.wifi;

/**
 * One Wi-Fi access point observed by a {@link WifiScanner} scan, in the shape Google's
 * Geolocation API expects for WiFi-based triangulation.
 *
 * @param bssid the access point's MAC address, formatted as lower-case colon-separated hex
 * @param signalStrengthDbm the observed signal strength in dBm (negative), or null if unknown
 * @param channel the Wi-Fi channel the access point was observed on, or null if unknown
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public record WifiNetwork(String bssid, Integer signalStrengthDbm, Integer channel) {
}
