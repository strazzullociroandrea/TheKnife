package com.strazzullo_marocco_sibilla_marin.app.storage;

/**
 * Exception thrown when an operation against the photo object storage fails.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class PhotoStorageException extends Exception {

    /**
     * PhotoStorageException constructor with a message and a cause.
     *
     * @param message description of the failure
     * @param cause   the underlying cause
     */
    public PhotoStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * PhotoStorageException constructor with a message only.
     *
     * @param message description of the failure
     */
    public PhotoStorageException(String message) {
        super(message);
    }
}
