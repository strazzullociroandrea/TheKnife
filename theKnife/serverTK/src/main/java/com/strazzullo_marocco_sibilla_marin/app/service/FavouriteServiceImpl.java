package com.strazzullo_marocco_sibilla_marin.app.service;

import com.strazzullo_marocco_sibilla_marin.app.dao.FavouriteDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.FavouriteDAOImpl;
import com.strazzullo_marocco_sibilla_marin.app.remote.FavouriteService;
import sibilla.LocationSearchResult;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation of {@link FavouriteService} RMI remote interface.
 * Decouples logic from database access via {@link FavouriteDAO}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class FavouriteServiceImpl extends UnicastRemoteObject implements FavouriteService {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(FavouriteServiceImpl.class.getName());

    private final FavouriteDAO favouriteDAO;

    /**
     * FavouriteServiceImpl constructor with a custom DAO injected.
     *
     * @param favouriteDAO the favourite DAO implementation to use
     * @throws RemoteException if RMI export fails
     */
    public FavouriteServiceImpl(FavouriteDAO favouriteDAO) throws RemoteException {
        super();
        this.favouriteDAO = favouriteDAO;
    }

    /**
     * FavouriteServiceImpl constructor. Exports the remote object and initializes the DAO layer.
     *
     * @throws RemoteException if RMI export fails
     */
    public FavouriteServiceImpl() throws RemoteException {
        this(new FavouriteDAOImpl());
    }

    /**
     * Function to add a location to a user's favourites.
     *
     * @param userId the id of the user
     * @param locationId the id of the location to favourite
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void addFavourite(String userId, String locationId) throws RemoteException {
        try {
            requireNonBlank(userId, "userId");
            requireNonBlank(locationId, "locationId");
            favouriteDAO.add(userId, locationId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in addFavourite service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to remove a location from a user's favourites.
     *
     * @param userId the id of the user
     * @param locationId the id of the location to remove
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void removeFavourite(String userId, String locationId) throws RemoteException {
        try {
            requireNonBlank(userId, "userId");
            requireNonBlank(locationId, "locationId");
            favouriteDAO.remove(userId, locationId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in removeFavourite service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to list the ids of every location a user has favourited.
     *
     * @param userId the id of the user
     * @return the set of favourited location ids, possibly empty
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public Set<String> listFavouriteLocationIds(String userId) throws RemoteException {
        try {
            requireNonBlank(userId, "userId");
            return favouriteDAO.listLocationIdsByUser(userId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in listFavouriteLocationIds service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to list a page of a user's favourite locations.
     *
     * @param userId the id of the user
     * @param pageNumber zero-based page index
     * @param pageSize number of favourites per page
     * @return the page's favourites, in most-recently-added-first order
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public List<LocationSearchResult> listFavourites(String userId, int pageNumber, int pageSize) throws RemoteException {
        try {
            requireNonBlank(userId, "userId");
            if (pageNumber < 0) throw new IllegalArgumentException("pageNumber cannot be negative, got " + pageNumber);
            if (pageSize <= 0) throw new IllegalArgumentException("pageSize must be positive, got " + pageSize);
            return favouriteDAO.listByUser(userId, pageSize, pageNumber * pageSize);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in listFavourites service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to validate that a required string argument is neither null nor empty.
     *
     * @param value the value to check
     * @param fieldName the argument's name, used in the exception message
     * @throws IllegalArgumentException if the value is null or empty
     */
    private void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is null or empty");
        }
    }
}
