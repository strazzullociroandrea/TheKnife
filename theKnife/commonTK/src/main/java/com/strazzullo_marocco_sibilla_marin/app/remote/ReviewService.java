package com.strazzullo_marocco_sibilla_marin.app.remote;

import marin.Review;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote service interface for managing reviews and their associated like system.
 *
 * This interface extends {@link java.rmi.Remote} to enable Remote Method Invocation (RMI).
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface ReviewService extends Remote {

    /**
     * Retrieves a specific review by its unique identifier.
     *
     * @param reviewId the identifier of the review to retrieve
     * @return the review object, or null if no review was found
     * @throws RemoteException if a network or communication error occurs
     */
    Review getReview(String reviewId) throws RemoteException;

    /**
     * Retrieves all reviews associated with a specific restaurant.
     *
     * @param restId the identifier of the restaurant
     * @return a list of reviews for the specified restaurant
     * @throws RemoteException if a network or communication error occurs
     */
    List<Review> getReviewsByRestaurant(String restId) throws RemoteException;

    /**
     * Retrieves all reviews written by a specific user.
     *
     * @param userId the identifier of the user
     * @return a list of reviews written by the specified user
     * @throws RemoteException if a network or communication error occurs
     */
    List<Review> getReviewsByUser(String userId) throws RemoteException;

    /**
     * Validates and publishes a new review in the system.
     *
     * @param rev the review object containing the data to be saved
     * @throws RemoteException if a network or communication error occurs
     */
    void publishReview(Review rev) throws RemoteException;

    /**
     * Validates and updates the content or ratings of an existing review.
     *
     * @param rev the review object containing the updated data
     * @throws RemoteException if a network or communication error occurs
     */
    void modifyReview(Review rev) throws RemoteException;

    /**
     * Removes a specific review from the system by its identifier.
     *
     * @param reviewId the identifier of the review to delete
     * @throws RemoteException if a network or communication error occurs
     */
    void removeReview(String reviewId) throws RemoteException;

    /**
     * Adds a like for a specific review on behalf of a user.
     *
     * @param userId the identifier of the user adding the like
     * @param reviewId the identifier of the review to like
     * @throws RemoteException if a network or communication error occurs
     */
    void addLikeToReview(String userId, String reviewId) throws RemoteException;

    /**
     * Removes a like from a specific review.
     *
     * @param userId the identifier of the user removing the like
     * @param reviewId the identifier of the review to unlike
     * @throws RemoteException if a network or communication error occurs
     */
    void removeLikeFromReview(String userId, String reviewId) throws RemoteException;

    /**
     * Retrieves the identifiers of all reviews liked by a specific user.
     *
     * @param userId the identifier of the user
     * @return a list of review identifiers liked by the user
     * @throws RemoteException if a network or communication error occurs
     */
    List<String> getUserLikedReviews(String userId) throws RemoteException;
}
