package com.strazzullo_marocco_sibilla_marin.app.remote;

import sibilla.Photo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote service interface exposing the restaurant location photo gallery via RMI.
 * Photos are publicly readable; upload and deletion are restricted to the
 * managers owning the restaurant the target location belongs to.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface PhotoService extends Remote {

    /**
     * Function to list every photo belonging to a location's public gallery.
     *
     * @param locationId the location id
     * @return the list of photos, ordered by upload date
     * @throws RemoteException if a remote communication error occurs
     */
    List<Photo> listPhotos(String locationId) throws RemoteException;

    /**
     * Function to upload a new photo to a location's gallery.
     * The raw bytes are stored on the S3-compatible object storage and only
     * the resulting public URL is persisted in the database.
     *
     * @param managerId      id of the manager performing the upload
     * @param locationId     id of the target location
     * @param fileName       original file name, used to infer the extension/content type
     * @param content        raw photo bytes
     * @return the persisted {@link Photo}
     * @throws RemoteException if the manager does not own the location, the upload fails,
     *                          or a remote communication error occurs
     */
    Photo uploadPhoto(String managerId, String locationId, String fileName, byte[] content) throws RemoteException;

    /**
     * Function to delete a photo from a location's gallery.
     * Removes both the object on the storage and the database row.
     *
     * @param managerId  id of the manager performing the deletion
     * @param photoId    id of the photo to delete
     * @throws RemoteException if the manager does not own the location, the photo does not exist,
     *                          or a remote communication error occurs
     */
    void deletePhoto(String managerId, String photoId) throws RemoteException;
}
