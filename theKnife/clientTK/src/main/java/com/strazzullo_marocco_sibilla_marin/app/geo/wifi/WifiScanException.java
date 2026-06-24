package com.strazzullo_marocco_sibilla_marin.app.geo.wifi;

/**
 * Thrown when the nearby Wi-Fi access points could not be listed: the platform's scanning
 * tool is missing, not permitted to run, or returned no usable output.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public class WifiScanException extends Exception {

    /**
     * @param message what went wrong
     * @param cause the underlying error, or null if none
     */
    public WifiScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
