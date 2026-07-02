package com.strazzullo_marocco_sibilla_marin.app.service;

import com.strazzullo_marocco_sibilla_marin.app.dao.RestaurantDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.RestaurantDAOImpl;
import com.strazzullo_marocco_sibilla_marin.app.remote.RestaurantService;
import sibilla.Restaurant;

import java.io.Serial;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation of {@link RestaurantService} RMI remote interface.
 * Exposes restaurant management to RMI clients: public reads, and update/deletion/owner
 * management restricted to the managers already owning the restaurant.
 * Decouples logic from database access via {@link RestaurantDAO}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class RestaurantServiceImpl extends UnicastRemoteObject implements RestaurantService {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(RestaurantServiceImpl.class.getName());

    private final RestaurantDAO restaurantDAO;

    /**
     * RestaurantServiceImpl constructor with a custom DAO injected.
     *
     * @param restaurantDAO the restaurant DAO implementation to use
     * @throws RemoteException if RMI export fails
     */
    public RestaurantServiceImpl(RestaurantDAO restaurantDAO) throws RemoteException {
        super();
        this.restaurantDAO = restaurantDAO;
    }

    /**
     * RestaurantServiceImpl constructor. Exports the remote object and initializes the DAO layer.
     *
     * @throws RemoteException if RMI export fails
     */
    public RestaurantServiceImpl() throws RemoteException {
        this(new RestaurantDAOImpl());
    }

    /**
     * Function to retrieve a restaurant by its unique identifier.
     *
     * @param restaurantId the restaurant id
     * @return the restaurant, or null if not found
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public Restaurant getRestaurant(String restaurantId) throws RemoteException {
        try {
            return restaurantDAO.findById(restaurantId).orElse(null);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in getRestaurant service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Function to list every restaurant owned by a specific user (manager).
     *
     * @param ownerId the id of the owner
     * @return the list of restaurants owned by the specified user
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public List<Restaurant> listRestaurantsByOwner(String ownerId) throws RemoteException {
        try {
            return restaurantDAO.findByOwner(ownerId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in listRestaurantsByOwner service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Function to create a new restaurant and link it to an initial owner.
     *
     * @param ownerId the id of the initial owner to link to the new restaurant
     * @param restaurant the restaurant to create
     * @return the created restaurant
     * @throws RemoteException if the operation fails or a remote communication error occurs
     */
    @Override
    public Restaurant createRestaurant(String ownerId, Restaurant restaurant) throws RemoteException {
        try {
            return restaurantDAO.create(restaurant, ownerId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in createRestaurant service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Function to update an existing restaurant, after verifying that the requesting
     * manager owns it.
     *
     * @param managerId the id of the manager performing the update
     * @param restaurant the restaurant with updated values
     * @return the updated restaurant
     * @throws RemoteException if the manager does not own the restaurant, the restaurant does not exist,
     *                          or a remote communication error occurs
     */
    @Override
    public Restaurant updateRestaurant(String managerId, Restaurant restaurant) throws RemoteException {
        try {
            requireOwner(managerId, restaurant.getId());
            return restaurantDAO.update(restaurant);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in updateRestaurant service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to delete a restaurant, after verifying that the requesting manager owns it.
     * Cascading deletes will remove associated owners, locations, and reviews.
     *
     * @param managerId the id of the manager performing the deletion
     * @param restaurantId the id of the restaurant to delete
     * @throws RemoteException if the manager does not own the restaurant, the restaurant does not exist,
     *                          or a remote communication error occurs
     */
    @Override
    public void deleteRestaurant(String managerId, String restaurantId) throws RemoteException {
        try {
            requireOwner(managerId, restaurantId);
            restaurantDAO.delete(restaurantId);
            LOGGER.info(() -> "Deleted restaurant " + restaurantId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in deleteRestaurant service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to add a user as a co-owner of a restaurant, after verifying that the
     * requesting manager already owns it.
     *
     * @param managerId the id of the manager performing the addition
     * @param restaurantId the restaurant id
     * @param newOwnerId the id of the user to add as owner
     * @return the updated restaurant with the new owner included
     * @throws RemoteException if the manager does not own the restaurant, the operation fails,
     *                          or a remote communication error occurs
     */
    @Override
    public Restaurant addOwner(String managerId, String restaurantId, String newOwnerId) throws RemoteException {
        try {
            requireOwner(managerId, restaurantId);
            return restaurantDAO.addOwnerToRestaurant(restaurantId, newOwnerId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in addOwner service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to remove a user from being a co-owner of a restaurant, after verifying
     * that the requesting manager already owns it.
     *
     * @param managerId the id of the manager performing the removal
     * @param restaurantId the restaurant id
     * @param ownerId the id of the owner to remove
     * @return the updated restaurant with the owner removed
     * @throws RemoteException if the manager does not own the restaurant, the operation fails,
     *                          or a remote communication error occurs
     */
    @Override
    public Restaurant removeOwner(String managerId, String restaurantId, String ownerId) throws RemoteException {
        try {
            requireOwner(managerId, restaurantId);
            return restaurantDAO.removeOwnerFromRestaurant(restaurantId, ownerId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in removeOwner service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to check whether a user is one of the owners (managers) of a restaurant.
     *
     * @param restaurantId the restaurant id
     * @param userId the user id
     * @return true if the user owns the restaurant
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public boolean isOwner(String restaurantId, String userId) throws RemoteException {
        try {
            return Boolean.TRUE.equals(restaurantDAO.isOwner(restaurantId, userId));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in isOwner service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Function to verify that a manager owns a restaurant.
     *
     * @param managerId the id of the manager
     * @param restaurantId the id of the restaurant
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the manager does not own the restaurant
     */
    private void requireOwner(String managerId, String restaurantId) throws SQLException {
        if (!Boolean.TRUE.equals(restaurantDAO.isOwner(restaurantId, managerId))) {
            throw new IllegalArgumentException("Manager " + managerId + " does not own restaurant " + restaurantId);
        }
    }
}
