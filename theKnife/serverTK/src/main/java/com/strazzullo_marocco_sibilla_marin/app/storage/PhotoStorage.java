package com.strazzullo_marocco_sibilla_marin.app.storage;

/**
 * Abstraction over the object storage backend used to persist restaurant
 * location photos. Decouples the rest of the application from the concrete
 * S3 client implementation.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface PhotoStorage {

    /**
     * Function to upload a photo's raw bytes to the object storage.
     *
     * @param locationId    id of the location the photo belongs to, used to namespace the object key
     * @param fileName      original file name, used to infer the file extension and content type
     * @param content       raw photo bytes
     * @return the public URL the uploaded photo is reachable at
     * @throws PhotoStorageException if the upload fails
     */
    String upload(String locationId, String fileName, byte[] content) throws PhotoStorageException;

    /**
     * Function to delete a previously uploaded photo, given its public URL.
     *
     * @param url the public URL returned by {@link #upload}
     * @throws PhotoStorageException if the deletion fails
     */
    void delete(String url) throws PhotoStorageException;
}
