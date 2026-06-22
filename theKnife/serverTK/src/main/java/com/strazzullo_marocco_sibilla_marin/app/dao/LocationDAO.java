package com.strazzullo_marocco_sibilla_marin.app.dao;

import marocco.SearchFilter;
import sibilla.Location;
import sibilla.LocationSearchResult;
import sibilla.Restaurant;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for performing search operations on restaurant location data.
 * Follows the Dependency Inversion Principle, decoupling business logic from JDBC implementation.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA - author of this file
 * @Author Marin Marco, 760622, VA
 */
public interface LocationDAO {

    /**
     * Function to dynamically search restaurant locations using criteria defined in the {@link SearchFilter}.
     * Calculates distance filtering if geographical reference points and radius are supplied.
     *
     * @param filter the filter criteria
     * @return a list of search results matching the filter criteria, each carrying the location plus its rating and restaurant info
     * @throws SQLException if a database query error occurs
     */
    List<LocationSearchResult> search(SearchFilter filter) throws SQLException;

    /**
     * Retrieves a location by its unique identifier.
     *
     * @param id the unique identifier of the location
     * @return the location with the specified id, or null if not found
     * @throws SQLException if a database query error occurs
     */
    Optional<Location> findById(String id) throws SQLException;

    /**
     * Retrieves all locations associated with a specific restaurant.
     *
     * @param restaurant the restaurant to find locations for
     * @return a list of locations belonging to the specified restaurant
     * @throws SQLException if a database query error occurs
     */
    List<Location> findByRestaurant(Restaurant restaurant) throws SQLException;

    /**
     * Creates a new location in the database.
     *
     * @param location the location object to create
     * @return the created location with database-generated attributes
     * @throws SQLException if a database operation error occurs
     */
    void create(Location location, String restaurantId) throws SQLException;

    /**
     * Updates an existing location in the database.
     *
     * @param location the location object with updated values
     * @return the updated location
     * @throws SQLException if a database operation error occurs
     */
    Location update(Location location) throws SQLException;

    /**
     * Deletes a location from the database.
     *
     * @param location the location object to delete
     * @throws SQLException if a database operation error occurs
     */
    void delete(Location location) throws SQLException;

}
