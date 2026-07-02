package com.strazzullo_marocco_sibilla_marin.app.remote;

import sibilla.LocationSearchResult;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

/**
 * Remote service interface exposing a customer's favourite locations via RMI.
 * Backed by the {@code favourite} join table between {@code app_user} and {@code location}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface FavouriteService extends Remote {

    /**
     * Function to add a location to a user's favourites. A no-op if it is already favourited.
     *
     * @param userId the id of the user
     * @param locationId the id of the location to favourite
     * @throws RemoteException if a remote communication error occurs
     */
    void addFavourite(String userId, String locationId) throws RemoteException;

    /**
     * Function to remove a location from a user's favourites. A no-op if it was not favourited.
     *
     * @param userId the id of the user
     * @param locationId the id of the location to remove
     * @throws RemoteException if a remote communication error occurs
     */
    void removeFavourite(String userId, String locationId) throws RemoteException;

    /**
     * Function to list the ids of every location a user has favourited, for cheaply checking many
     * locations' favourite state at once (e.g. every card on a results grid) without one RMI call
     * per location.
     *
     * @param userId the id of the user
     * @return the set of favourited location ids, possibly empty
     * @throws RemoteException if a remote communication error occurs
     */
    Set<String> listFavouriteLocationIds(String userId) throws RemoteException;

    /**
     * Function to list a page of a user's favourite locations, most recently added first, plus
     * their rating and restaurant info.
     *
     * @param userId the id of the user
     * @param pageNumber zero-based page index
     * @param pageSize number of favourites per page
     * @return the page's favourites, in most-recently-added-first order
     * @throws RemoteException if a remote communication error occurs
     */
    List<LocationSearchResult> listFavourites(String userId, int pageNumber, int pageSize) throws RemoteException;
}
