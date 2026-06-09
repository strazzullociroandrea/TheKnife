package com.strazzullo_marocco_sibilla_marin.app.remote;

import marocco.SearchFilter;
import sibilla.Location;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote service interface exposing customer-specific actions via RMI.
 * Focused on the restaurant branch search and filtering functionality.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface CustomerService extends Remote {

    /**
     * Function to search for restaurant locations based on a search filter.
     *
     * @param filter the search criteria including cuisines, prices, location, and distance
     * @return a list of locations matching the filter criteria
     * @throws RemoteException if a remote communication error occurs
     */
    List<Location> searchLocations(SearchFilter filter) throws RemoteException;
}
