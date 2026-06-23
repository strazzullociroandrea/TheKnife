package com.strazzullo_marocco_sibilla_marin.app.remote;

import sibilla.Restaurant;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote service interface exposing restaurant management via RMI.
 * Restaurants are publicly readable; update, deletion, and owner management are
 * restricted to the managers already owning the restaurant.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface RestaurantService extends Remote {

    /**
     * Function to retrieve a restaurant by its unique identifier.
     *
     * @param restaurantId the restaurant id
     * @return the restaurant, or null if not found
     * @throws RemoteException if a remote communication error occurs
     */
    Restaurant getRestaurant(String restaurantId) throws RemoteException;

    /**
     * Function to list every restaurant owned by a specific user (manager).
     *
     * @param ownerId the id of the owner
     * @return the list of restaurants owned by the specified user
     * @throws RemoteException if a remote communication error occurs
     */
    List<Restaurant> listRestaurantsByOwner(String ownerId) throws RemoteException;

    /**
     * Function to create a new restaurant and link it to an initial owner.
     *
     * @param ownerId the id of the initial owner to link to the new restaurant
     * @param restaurant the restaurant to create
     * @return the created restaurant
     * @throws RemoteException if the operation fails or a remote communication error occurs
     */
    Restaurant createRestaurant(String ownerId, Restaurant restaurant) throws RemoteException;

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
    Restaurant updateRestaurant(String managerId, Restaurant restaurant) throws RemoteException;

    /**
     * Function to delete a restaurant, after verifying that the requesting manager owns it.
     * Cascading deletes will remove associated owners, locations, and reviews.
     *
     * @param managerId the id of the manager performing the deletion
     * @param restaurantId the id of the restaurant to delete
     * @throws RemoteException if the manager does not own the restaurant, the restaurant does not exist,
     *                          or a remote communication error occurs
     */
    void deleteRestaurant(String managerId, String restaurantId) throws RemoteException;

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
    Restaurant addOwner(String managerId, String restaurantId, String newOwnerId) throws RemoteException;

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
    Restaurant removeOwner(String managerId, String restaurantId, String ownerId) throws RemoteException;

    /**
     * Function to check whether a user is one of the owners (managers) of a restaurant.
     *
     * @param restaurantId the restaurant id
     * @param userId the user id
     * @return true if the user owns the restaurant
     * @throws RemoteException if a remote communication error occurs
     */
    boolean isOwner(String restaurantId, String userId) throws RemoteException;
}
