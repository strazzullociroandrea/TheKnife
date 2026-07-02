package com.strazzullo_marocco_sibilla_marin.app.dao;

import sibilla.LocationSearchResult;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Data Access Object (DAO) interface for a customer's favourite locations, backed by the
 * {@code favourite} join table between {@code app_user} and {@code location}.
 * Follows the Dependency Inversion Principle, decoupling business logic from JDBC implementation.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface FavouriteDAO {

    /**
     * Adds a location to a user's favourites. A no-op if it is already favourited.
     *
     * @param userId the id of the user
     * @param locationId the id of the location to favourite
     * @throws SQLException if a database operation error occurs
     */
    void add(String userId, String locationId) throws SQLException;

    /**
     * Removes a location from a user's favourites. A no-op if it was not favourited.
     *
     * @param userId the id of the user
     * @param locationId the id of the location to remove
     * @throws SQLException if a database operation error occurs
     */
    void remove(String userId, String locationId) throws SQLException;

    /**
     * Retrieves the ids of every location a user has favourited.
     *
     * @param userId the id of the user
     * @return the set of favourited location ids, possibly empty
     * @throws SQLException if a database query error occurs
     */
    Set<String> listLocationIdsByUser(String userId) throws SQLException;

    /**
     * Retrieves a page of a user's favourite locations, most recently added first, plus their
     * rating and restaurant info.
     *
     * @param userId the id of the user
     * @param limit the maximum number of favourites to return
     * @param offset the number of favourites to skip, for pagination
     * @return the page's favourites, in most-recently-added-first order
     * @throws SQLException if a database query error occurs
     */
    List<LocationSearchResult> listByUser(String userId, int limit, int offset) throws SQLException;
}
