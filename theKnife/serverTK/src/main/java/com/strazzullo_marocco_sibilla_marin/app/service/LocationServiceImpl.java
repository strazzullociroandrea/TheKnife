package com.strazzullo_marocco_sibilla_marin.app.service;

import com.strazzullo_marocco_sibilla_marin.app.dao.LocationDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.RestaurantDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.LocationDAOImpl;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.RestaurantDAOImpl;
import com.strazzullo_marocco_sibilla_marin.app.remote.LocationService;
import sibilla.Location;
import sibilla.Restaurant;

import java.io.Serial;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation of {@link LocationService} RMI remote interface.
 * Exposes restaurant location management to RMI clients: public reads, and
 * creation/update/deletion restricted to the managers owning the restaurant
 * the location belongs to. Decouples logic from database access via
 * {@link LocationDAO} and {@link RestaurantDAO}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class LocationServiceImpl extends UnicastRemoteObject implements LocationService {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(LocationServiceImpl.class.getName());

    private final LocationDAO locationDAO;
    private final RestaurantDAO restaurantDAO;

    /**
     * LocationServiceImpl constructor with custom DAOs injected.
     *
     * @param locationDAO the location DAO implementation to use
     * @param restaurantDAO the restaurant DAO implementation to use, for ownership checks
     * @throws RemoteException if RMI export fails
     */
    public LocationServiceImpl(LocationDAO locationDAO, RestaurantDAO restaurantDAO) throws RemoteException {
        super();
        this.locationDAO = locationDAO;
        this.restaurantDAO = restaurantDAO;
    }

    /**
     * LocationServiceImpl constructor. Exports the remote object and initializes the DAO layer.
     *
     * @throws RemoteException if RMI export fails
     */
    public LocationServiceImpl() throws RemoteException {
        this(new LocationDAOImpl(), new RestaurantDAOImpl());
    }

    /**
     * Function to retrieve a location by its unique identifier.
     *
     * @param locationId the location id
     * @return the location, or null if not found
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public Location getLocation(String locationId) throws RemoteException {
        try {
            return locationDAO.findById(locationId).orElse(null);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in getLocation service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Function to list every location belonging to a restaurant.
     *
     * @param restaurantId the restaurant id
     * @return the list of locations belonging to the restaurant
     * @throws RemoteException if the restaurant does not exist or a remote communication error occurs
     */
    @Override
    public List<Location> listLocationsByRestaurant(String restaurantId) throws RemoteException {
        try {
            Restaurant restaurant = restaurantDAO.findById(restaurantId)
                    .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + restaurantId));
            return locationDAO.findByRestaurant(restaurant);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in listLocationsByRestaurant service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to create a new location for a restaurant, after verifying that the
     * requesting manager owns the restaurant.
     *
     * @param managerId the id of the manager performing the creation
     * @param restaurantId the id of the restaurant the new location belongs to
     * @param location the location to create
     * @return the created location
     * @throws RemoteException if the manager does not own the restaurant, the operation fails,
     *                          or a remote communication error occurs
     */
    @Override
    public Location createLocation(String managerId, String restaurantId, Location location) throws RemoteException {
        try {
            requireOwnedRestaurant(managerId, restaurantId);
            locationDAO.create(location, restaurantId);
            LOGGER.info(() -> "Created location " + location.getId() + " for restaurant " + restaurantId);
            return location;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in createLocation service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to update an existing location, after verifying that the requesting
     * manager owns the restaurant the location belongs to.
     *
     * @param managerId the id of the manager performing the update
     * @param location the location with updated values
     * @return the updated location
     * @throws RemoteException if the manager does not own the location, the location does not exist,
     *                          or a remote communication error occurs
     */
    @Override
    public Location updateLocation(String managerId, Location location) throws RemoteException {
        try {
            requireOwnedLocation(managerId, location.getId());
            return locationDAO.update(location);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in updateLocation service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to delete a location, after verifying that the requesting manager
     * owns the restaurant the location belongs to.
     *
     * @param managerId the id of the manager performing the deletion
     * @param locationId the id of the location to delete
     * @throws RemoteException if the manager does not own the location, the location does not exist,
     *                          or a remote communication error occurs
     */
    @Override
    public void deleteLocation(String managerId, String locationId) throws RemoteException {
        try {
            requireOwnedLocation(managerId, locationId);
            Location location = locationDAO.findById(locationId)
                    .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));
            locationDAO.delete(location);
            LOGGER.info(() -> "Deleted location " + locationId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in deleteLocation service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
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
    private void requireOwnedRestaurant(String managerId, String restaurantId) throws SQLException {
        if (!Boolean.TRUE.equals(restaurantDAO.isOwner(restaurantId, managerId))) {
            throw new IllegalArgumentException("Manager " + managerId + " does not own restaurant " + restaurantId);
        }
    }

    /**
     * Function to verify that a manager owns the restaurant a location belongs to.
     *
     * @param managerId the id of the manager
     * @param locationId the id of the location
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the location does not exist or the manager does not own it
     */
    private void requireOwnedLocation(String managerId, String locationId) throws SQLException {
        Optional<String> restaurantId = locationDAO.findRestaurantIdById(locationId);
        if (restaurantId.isEmpty()) {
            throw new IllegalArgumentException("Location not found: " + locationId);
        }
        requireOwnedRestaurant(managerId, restaurantId.get());
    }
}
