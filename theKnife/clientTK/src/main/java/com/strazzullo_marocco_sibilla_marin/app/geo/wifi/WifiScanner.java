package com.strazzullo_marocco_sibilla_marin.app.geo.wifi;

import java.util.List;

/**
 * Abstraction over an OS-specific Wi-Fi access point scan, used to feed Google's Geolocation
 * API a list of nearby access points for WiFi-based positioning. Implementations shell out to
 * whatever scanning tool ships with the host OS, since the JDK has no portable Wi-Fi scan API.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public interface WifiScanner {

    /**
     * Function to list the Wi-Fi access points currently visible to this machine.
     *
     * @return the observed access points; never null, but may be empty
     * @throws WifiScanException if the scan could not be run or its output could not be parsed
     */
    List<WifiNetwork> scan() throws WifiScanException;
}
