package com.strazzullo_marocco_sibilla_marin.app.dao;

import sibilla.Photo;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for the {@code photo} table.
 * Follows the Dependency Inversion Principle, decoupling business logic from JDBC implementation.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface PhotoDAO {

    /**
     * Function to persist a new photo row.
     *
     * @param photo the photo to insert
     * @throws SQLException if a database error occurs
     */
    void insert(Photo photo) throws SQLException;

    /**
     * Function to list every photo belonging to a location, ordered by upload date.
     *
     * @param locationId the location id
     * @return the list of photos
     * @throws SQLException if a database error occurs
     */
    List<Photo> findByLocation(String locationId) throws SQLException;

    /**
     * Function to find a single photo by id.
     *
     * @param photoId the photo id
     * @return the photo, if found
     * @throws SQLException if a database error occurs
     */
    Optional<Photo> findById(String photoId) throws SQLException;

    /**
     * Function to find the id of the restaurant a photo's location belongs to,
     * used to verify manager ownership before allowing upload/deletion.
     *
     * @param locationId the location id
     * @return the owning restaurant id, if the location exists
     * @throws SQLException if a database error occurs
     */
    Optional<String> findRestaurantIdByLocation(String locationId) throws SQLException;

    /**
     * Function to check whether a user is one of the owners (managers) of a restaurant.
     *
     * @param restaurantId the restaurant id
     * @param userId       the user id
     * @return true if the user owns the restaurant
     * @throws SQLException if a database error occurs
     */
    boolean isRestaurantOwner(String restaurantId, String userId) throws SQLException;

    /**
     * Function to delete a photo row by id.
     *
     * @param photoId the photo id
     * @throws SQLException if a database error occurs
     */
    void delete(String photoId) throws SQLException;
}
