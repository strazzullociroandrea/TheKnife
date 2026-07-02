package com.strazzullo_marocco_sibilla_marin.app.geo;

/**
 * Exception thrown when an address cannot be resolved to coordinates.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class GeocodingException extends Exception {

    /**
     * GeocodingException constructor with a message and a cause.
     *
     * @param message description of the failure
     * @param cause   the underlying cause
     */
    public GeocodingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * GeocodingException constructor with a message only.
     *
     * @param message description of the failure
     */
    public GeocodingException(String message) {
        super(message);
    }
}
