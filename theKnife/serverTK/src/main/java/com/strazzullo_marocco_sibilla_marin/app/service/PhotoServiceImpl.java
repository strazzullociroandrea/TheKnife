package com.strazzullo_marocco_sibilla_marin.app.service;

import com.strazzullo_marocco_sibilla_marin.app.dao.PhotoDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.PhotoDAOImpl;
import com.strazzullo_marocco_sibilla_marin.app.remote.PhotoService;
import com.strazzullo_marocco_sibilla_marin.app.storage.PhotoStorage;
import com.strazzullo_marocco_sibilla_marin.app.storage.PhotoStorageException;
import sibilla.Photo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation of {@link PhotoService} RMI remote interface.
 * Exposes the location photo gallery to RMI clients: public listing for
 * customers, and upload/deletion restricted to the managers owning the
 * restaurant the location belongs to.
 * Decouples logic from database access via {@link PhotoDAO} and from the
 * object storage backend via {@link PhotoStorage}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class PhotoServiceImpl extends UnicastRemoteObject implements PhotoService {

    /**
     * Unique identifier for serialization to ensure that a loaded class corresponds
     * exactly to the serialized object.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The photo DAO instance.
     */
    private final PhotoDAO photoDAO;

    /**
     * The object storage backend instance.
     */
    private final PhotoStorage photoStorage;

    /**
     * PhotoServiceImpl constructor with custom DAO and storage injected.
     *
     * @param photoDAO     the photo DAO implementation to use
     * @param photoStorage the object storage backend to use
     * @throws RemoteException if RMI export fails
     */
    public PhotoServiceImpl(PhotoDAO photoDAO, PhotoStorage photoStorage) throws RemoteException {
        super();
        this.photoDAO = photoDAO;
        this.photoStorage = photoStorage;
    }

    /**
     * PhotoServiceImpl constructor. Exports the remote object and initializes the DAO layer.
     *
     * @param photoStorage the object storage backend to use
     * @throws RemoteException if RMI export fails
     */
    public PhotoServiceImpl(PhotoStorage photoStorage) throws RemoteException {
        this(new PhotoDAOImpl(), photoStorage);
    }

    /**
     * Function to list every photo belonging to a location's public gallery.
     *
     * @param locationId the location id
     * @return the list of photos, ordered by upload date
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public List<Photo> listPhotos(String locationId) throws RemoteException {
        try {
            return photoDAO.findByLocation(locationId);
        } catch (SQLException e) {
            System.err.println("SQLException encountered in listPhotos service method: " + e.getMessage());
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Function to upload a new photo to a location's gallery, after verifying
     * that the requesting manager owns the restaurant the location belongs to.
     *
     * @param managerId      id of the manager performing the upload
     * @param locationId     id of the target location
     * @param fileName       original file name, used to infer the extension/content type
     * @param content        raw photo bytes
     * @return the persisted {@link Photo}
     * @throws RemoteException if the manager does not own the location, the upload fails,
     *                          or a remote communication error occurs
     */
    @Override
    public Photo uploadPhoto(String managerId, String locationId, String fileName, byte[] content) throws RemoteException {
        try {
            String restaurantId = requireOwnedLocation(managerId, locationId);
            String url = photoStorage.upload(locationId, fileName, content);
            Photo photo = new Photo(locationId, url);
            photoDAO.insert(photo);
            System.out.println("Uploaded photo " + photo.getId() + " for location " + locationId
                    + " (restaurant " + restaurantId + ")");
            return photo;
        } catch (SQLException e) {
            System.err.println("SQLException encountered in uploadPhoto service method: " + e.getMessage());
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (PhotoStorageException e) {
            System.err.println("PhotoStorageException encountered in uploadPhoto service method: " + e.getMessage());
            throw new RemoteException("Object storage exception occurred on the server side.", e);
        }
    }

    /**
     * Function to delete a photo from a location's gallery, after verifying
     * that the requesting manager owns the restaurant the location belongs to.
     *
     * @param managerId  id of the manager performing the deletion
     * @param photoId    id of the photo to delete
     * @throws RemoteException if the manager does not own the location, the photo does not exist,
     *                          or a remote communication error occurs
     */
    @Override
    public void deletePhoto(String managerId, String photoId) throws RemoteException {
        try {
            Photo photo = photoDAO.findById(photoId)
                    .orElseThrow(() -> new IllegalArgumentException("Photo not found: " + photoId));
            requireOwnedLocation(managerId, photo.getLocationId());
            photoStorage.delete(photo.getUrl());
            photoDAO.delete(photoId);
            System.out.println("Deleted photo " + photoId);
        } catch (SQLException e) {
            System.err.println("SQLException encountered in deletePhoto service method: " + e.getMessage());
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (PhotoStorageException e) {
            System.err.println("PhotoStorageException encountered in deletePhoto service method: " + e.getMessage());
            throw new RemoteException("Object storage exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to verify that a manager owns the restaurant a location belongs to.
     *
     * @param managerId  id of the manager
     * @param locationId id of the location
     * @return the owning restaurant id
     * @throws SQLException             if a database error occurs
     * @throws IllegalArgumentException if the location does not exist or the manager does not own it
     */
    private String requireOwnedLocation(String managerId, String locationId) throws SQLException {
        Optional<String> restaurantId = photoDAO.findRestaurantIdByLocation(locationId);
        if (restaurantId.isEmpty()) {
            throw new IllegalArgumentException("Location not found: " + locationId);
        }
        if (!photoDAO.isRestaurantOwner(restaurantId.get(), managerId)) {
            throw new IllegalArgumentException("Manager " + managerId + " does not own location " + locationId);
        }
        return restaurantId.get();
    }
}
