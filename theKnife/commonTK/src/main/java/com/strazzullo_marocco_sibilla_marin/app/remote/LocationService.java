package com.strazzullo_marocco_sibilla_marin.app.remote;

import sibilla.Location;
import sibilla.LocationSearchResult;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote service interface exposing restaurant location management via RMI.
 * Locations are publicly readable; creation, update, and deletion are restricted to
 * the managers owning the restaurant the location belongs to.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface LocationService extends Remote {

    /**
     * Function to retrieve a location by its unique identifier.
     *
     * @param locationId the location id
     * @return the location, or null if not found
     * @throws RemoteException if a remote communication error occurs
     */
    Location getLocation(String locationId) throws RemoteException;

    /**
     * Function to retrieve a location by its unique identifier, plus its rating and parent
     * restaurant info (name, cuisine), the same shape {@link CustomerService#searchLocations}
     * returns. Used where a location is known only by id (e.g. from a {@code Booking}) but its
     * restaurant name is needed for display.
     *
     * @param locationId the location id
     * @return the location's search result, or null if not found
     * @throws RemoteException if a remote communication error occurs
     */
    LocationSearchResult getLocationSearchResult(String locationId) throws RemoteException;

    /**
     * Function to list every location belonging to a restaurant.
     *
     * @param restaurantId the restaurant id
     * @return the list of locations belonging to the restaurant
     * @throws RemoteException if the restaurant does not exist or a remote communication error occurs
     */
    List<Location> listLocationsByRestaurant(String restaurantId) throws RemoteException;

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
    Location createLocation(String managerId, String restaurantId, Location location) throws RemoteException;

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
    Location updateLocation(String managerId, Location location) throws RemoteException;

    /**
     * Function to delete a location, after verifying that the requesting manager
     * owns the restaurant the location belongs to.
     *
     * @param managerId the id of the manager performing the deletion
     * @param locationId the id of the location to delete
     * @throws RemoteException if the manager does not own the location, the location does not exist,
     *                          or a remote communication error occurs
     */
    void deleteLocation(String managerId, String locationId) throws RemoteException;
}
