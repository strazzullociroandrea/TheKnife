package com.strazzullo_marocco_sibilla_marin.app.service;

import com.strazzullo_marocco_sibilla_marin.app.dao.LocationDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.LocationDAOImpl;
import marocco.SearchFilter;
import com.strazzullo_marocco_sibilla_marin.app.remote.CustomerService;
import sibilla.LocationSearchResult;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation of {@link CustomerService} RMI remote interface.
 * Exposes the location searching and filtering capability to RMI clients.
 * Decouples logic from database access by querying the database through the {@link LocationDAO} interface.
 * Extends {@link UnicastRemoteObject} to participate in RMI remote runtime communication.
 *
 * @version 1.1
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class CustomerServiceImpl extends UnicastRemoteObject implements CustomerService {
    private static final long serialVersionUID = 2L;

    private static final Logger LOGGER = Logger.getLogger(CustomerServiceImpl.class.getName());

    private final LocationDAO locationDAO;

    /**
     * CustomerServiceImpl constructor. Exports the remote object and initializes the DAO layer.
     *
     * @throws RemoteException if RMI export fails
     */
    public CustomerServiceImpl() throws RemoteException {
        super();
        this.locationDAO = new LocationDAOImpl();
    }

    /**
     * CustomerServiceImpl constructor with custom DAO injected.
     *
     * @param locationDAO the location DAO implementation to use
     * @throws RemoteException if RMI export fails
     */
    public CustomerServiceImpl(LocationDAO locationDAO) throws RemoteException {
        super();
        this.locationDAO = locationDAO;
    }

    /**
     * Function to search for restaurant locations based on a search filter.
     *
     * @param filter the search criteria including cuisines, prices, location, and distance
     * @return a list of search results matching the filter criteria
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public List<LocationSearchResult> searchLocations(SearchFilter filter) throws RemoteException {
        if (filter == null) {
            filter = new SearchFilter.Builder().build();
        }
        LOGGER.fine(() -> "Executing location search using filter parameters: " + filter);
        try {
            return locationDAO.search(filter);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in searchLocations service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }
}
