package com.strazzullo_marocco_sibilla_marin.app.geo.wifi;

/**
 * Detects the placeholder BSSID ({@code 00:00:00:00:00:00}) OS Wi-Fi scan tools report in place
 * of a real access point address when the calling process lacks Location Services permission
 * (required on macOS, and increasingly elsewhere, to read BSSIDs at all). Access points reported
 * this way carry no positioning information and must be dropped rather than sent to Google's
 * Geolocation API, which would otherwise silently fall back to a coarse IP-based estimate instead
 * of failing loudly.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
final class MaskedBssid {

    private static final String PLACEHOLDER = "00:00:00:00:00:00";

    private MaskedBssid() {
    }

    /**
     * Function to check whether a BSSID is the OS's "permission not granted" placeholder.
     *
     * @param bssid the BSSID to check, already lower-cased
     * @return true if the BSSID is the placeholder and carries no real positioning information
     */
    static boolean isMasked(String bssid) {
        return PLACEHOLDER.equals(bssid);
    }
}
