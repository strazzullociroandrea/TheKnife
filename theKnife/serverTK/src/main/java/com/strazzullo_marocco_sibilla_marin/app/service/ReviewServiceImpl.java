package com.strazzullo_marocco_sibilla_marin.app.service;

import com.strazzullo_marocco_sibilla_marin.app.dao.ReviewDAO;
import com.strazzullo_marocco_sibilla_marin.app.remote.ReviewService;
import marin.Review;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;

/**
 * Concrete implementation of the remote {@link ReviewService} interface.
 *
 * This class extends {@link java.rmi.server.UnicastRemoteObject} to automatically
 * export itself as a remote object upon instantiation. It encapsulates the core
 * business logic rules, applies validation checks, and delegates data persistence
 * operations to {@link ReviewDAO}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class ReviewServiceImpl extends UnicastRemoteObject implements ReviewService {

    private final ReviewDAO reviewDAO;

    /**
     * Constructs a new ReviewServiceImpl with the specified data access object.
     *
     * @param reviewDAO the data access object used to handle persistence operations
     * @throws RemoteException if the remote object cannot be exported during initialization
     */
    public ReviewServiceImpl(ReviewDAO reviewDAO) throws RemoteException {
        super();
        this.reviewDAO = reviewDAO;
    }

    /**
     * Retrieves a specific review by its unique identifier.
     *
     * @param reviewId the identifier of the review to retrieve
     * @return the review object, or null if no review was found
     * @throws RemoteException if a database access error occurs on the server side
     */
    @Override
    public Review getReview(String reviewId) throws RemoteException {
        try {
            return reviewDAO.getReviewById(reviewId);
        } catch (SQLException e) {
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Retrieves all reviews associated with a specific restaurant.
     *
     * @param restId the identifier of the restaurant
     * @return a list of reviews for the specified restaurant
     * @throws RemoteException if a database access error occurs on the server side
     */
    @Override
    public List<Review> getReviewsByRestaurant(String restId) throws RemoteException {
        try {
            return reviewDAO.findByRestaurant(restId);
        } catch (SQLException e) {
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Retrieves all reviews written by a specific user.
     *
     * @param userId the identifier of the user
     * @return a list of reviews written by the specified user
     * @throws RemoteException if a database access error occurs on the server side
     */
    @Override
    public List<Review> getReviewsByUser(String userId) throws RemoteException {
        try {
            return reviewDAO.findByUser(userId);
        } catch (SQLException e) {
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Validates and publishes a new review in the system.
     *
     * @param rev the review object containing the data to be saved
     * @throws RemoteException if validation fails or a database error occurs on the server side
     */
    @Override
    public void publishReview(Review rev) throws RemoteException {
        try {
            rev.validateAllRatings();
            reviewDAO.save(rev);
        } catch (SQLException e) {
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Validates and updates the content or ratings of an existing review.
     *
     * @param rev the review object containing the updated data
     * @throws RemoteException if the review is not found, validation fails, or a database error occurs
     */
    @Override
    public void modifyReview(Review rev) throws RemoteException {
        try {
            Review exist = reviewDAO.getReviewById(rev.getReviewId());
            if (exist == null) {
                throw new SQLException("Review not found.");
            }
            rev.validateAllRatings();
            reviewDAO.update(rev);
        } catch (SQLException e) {
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Removes a specific review from the system by its identifier.
     *
     * @param reviewId the identifier of the review to delete
     * @throws RemoteException if the review is not found or a database error occurs on the server side
     */
    @Override
    public void removeReview(String reviewId) throws RemoteException {
        try {
            Review exist = reviewDAO.getReviewById(reviewId);
            if (exist == null) {
                throw new SQLException("Review not found.");
            }
            reviewDAO.delete(reviewId);
        } catch (SQLException e) {
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Adds a like for a specific review on behalf of a user.
     *
     * @param userId the identifier of the user adding the like
     * @param reviewId the identifier of the review to like
     * @throws RemoteException if a database access error occurs on the server side
     */
    @Override
    public void addLikeToReview(String userId, String reviewId) throws RemoteException {
        try {
            reviewDAO.addLike(userId, reviewId);
        } catch (SQLException e) {
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Removes a previously registered like from a specific review.
     *
     * @param userId the identifier of the user removing the like
     * @param reviewId the identifier of the review to unlike
     * @throws RemoteException if a database access error occurs on the server side
     */
    @Override
    public void removeLikeFromReview(String userId, String reviewId) throws RemoteException {
        try {
            reviewDAO.removeLike(userId, reviewId);
        } catch (SQLException e) {
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Retrieves the identifiers of all reviews liked by a specific user.
     *
     * @param userId the identifier of the user
     * @return a list of review identifiers liked by the user
     * @throws RemoteException if a database access error occurs on the server side
     */
    @Override
    public List<String> getUserLikedReviews(String userId) throws RemoteException {
        try {
            return reviewDAO.getLikesByUser(userId);
        } catch (SQLException e) {
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }
}
