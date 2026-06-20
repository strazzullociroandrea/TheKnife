package com.strazzullo_marocco_sibilla_marin.app.dao;

import sibilla.Restaurant;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object (DAO) interface for restaurant entity operations.
 * Provides abstraction for database interactions including CRUD operations,
 * owner management, and restaurant queries.
 * Follows the Data Access Object design pattern and Dependency Inversion Principle.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA - author of this file
 * @Author Marin Marco, 760622, VA
 */
public interface RestaurantDAO {

    /**
     * Retrieves a restaurant by its unique identifier.
     *
     * @param id the unique identifier of the restaurant
     * @return an Optional containing the restaurant if found, or empty if not found
     * @throws SQLException if a database query error occurs
     */
    Optional<Restaurant> findById(String id) throws SQLException;

    /**
     * Retrieves all restaurants owned by a specific user (manager).
     *
     * @param id the unique identifier of the owner (user)
     * @return a list of Restaurant objects owned by the specified user
     * @throws SQLException if a database query error occurs
     */
    List<Restaurant> findByOwner(String id) throws SQLException;

    /**
     * Creates a new restaurant in the database and links it to an initial owner.
     *
     * @param restaurant the restaurant object to create
     * @param ownerId the user id of the initial owner to link to this restaurant
     * @return the created restaurant with database-generated attributes
     * @throws SQLException if a database operation error occurs
     */
    Restaurant create(Restaurant restaurant, String ownerId) throws SQLException;

    /**
     * Updates an existing restaurant's information in the database.
     *
     * @param restaurant the restaurant object with updated values
     * @return the updated restaurant
     * @throws SQLException if a database operation error occurs
     */
    Restaurant update(Restaurant restaurant) throws SQLException;

    /**
     * Deletes a restaurant from the database by its id.
     * Cascading deletes will remove associated owners, locations, and reviews.
     *
     * @param id the unique identifier of the restaurant to delete
     * @throws SQLException if a database operation error occurs
     */
    void delete(String id) throws SQLException;

    /**
     * Adds a user as an owner of a restaurant.
     *
     * @param restaurantId the unique identifier of the restaurant
     * @param ownerId the unique identifier of the user to add as owner
     * @return the updated restaurant with the new owner included in the owners list
     * @throws SQLException if a database operation error occurs
     */
    Restaurant addOwnerToRestaurant(String restaurantId, String ownerId) throws SQLException;

    /**
     * Removes a user from being an owner of a restaurant.
     *
     * @param restaurantId the unique identifier of the restaurant
     * @param ownerId the unique identifier of the user to remove from owners
     * @return the updated restaurant with the owner removed from the owners list
     * @throws SQLException if a database operation error occurs
     */
    Restaurant removeOwnerFromRestaurant(String restaurantId, String ownerId) throws SQLException;

    /**
     * Checks whether a specific user is an owner of a restaurant.
     *
     * @param restaurantId the unique identifier of the restaurant
     * @param ownerId the unique identifier of the user
     * @return true if the user is an owner of the restaurant; false otherwise
     * @throws SQLException if a database query error occurs
     */
    Boolean isOwner(String restaurantId, String ownerId) throws SQLException;

}
