package com.strazzullo_marocco_sibilla_marin.app.dao;

import marin.Review;

import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) interface for managing review persistence.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface ReviewDAO {

    /**
     * Retrieves all reviews associated with a specific restaurant.
     *
     * @param restId the identifier of the restaurant
     * @return a list of reviews for the specified restaurant
     * @throws SQLException if an error occurs while accessing the data source
     */
    List<Review> findByRestaurant(String restId) throws SQLException;

    /**
     * Retrieves all reviews written by a specific user.
     *
     * @param userId the identifier of the user
     * @return a list of reviews written by the specified user
     * @throws SQLException if an error occurs while accessing the data source
     */
    List<Review> findByUser(String userId) throws SQLException;

    /**
     * Saves a new review in the data source.
     *
     * @param rev the review to be saved
     * @throws SQLException if an error occurs while accessing the data source
     */
    void save(Review rev) throws SQLException;

    /**
     * Updates an existing review in the data source.
     *
     * @param rev the review containing the updated information
     * @throws SQLException if an error occurs while accessing the data source
     */
    void update(Review rev) throws SQLException;

    /**
     * Deletes a review from the data source.
     *
     * @param reviewId the identifier of the review to delete
     * @throws SQLException if an error occurs while accessing the data source
     */
    void delete(String reviewId) throws SQLException;

    /**
     * Adds a like to a review on behalf of a user.
     *
     * @param userId the identifier of the user adding the like
     * @param reviewId the identifier of the review to like
     * @throws SQLException if an error occurs while accessing the data source
     */
    void addLike(String userId, String reviewId) throws SQLException;

}