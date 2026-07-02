package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import marin.Review;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Section listing every {@link Review} written for a location, loaded asynchronously via {@link
 * com.strazzullo_marocco_sibilla_marin.app.remote.ReviewService}, with the current user's already
 * -liked reviews pre-marked so {@link ReviewCard}'s like button starts in the right state.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class ReviewsSection extends VBox {

    private static final Logger LOGGER = Logger.getLogger(ReviewsSection.class.getName());

    private final VBox list = new VBox(16);
    private final ComboBox<ReviewSortOrder> sortPicker = new ComboBox<>();
    private final String currentUserId;
    private ReviewsData data = new ReviewsData(List.of(), Set.of());

    /**
     * ReviewsSection constructor. Starts loading the location's reviews immediately.
     *
     * @param locationId the location whose reviews to show
     * @param currentUserId the id of the logged-in user, used to track and toggle likes
     */
    public ReviewsSection(String locationId, String currentUserId) {
        super(16);
        this.currentUserId = currentUserId;

        Label title = new Label("Recensioni", new FontIcon(Feather.MESSAGE_CIRCLE));
        title.getStyleClass().add(Styles.TITLE_3);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        sortPicker.getItems().addAll(ReviewSortOrder.values());
        sortPicker.setValue(ReviewSortOrder.MOST_RECENT);
        sortPicker.setVisible(false);
        sortPicker.setManaged(false);
        sortPicker.setOnAction(e -> renderReviews());

        HBox headerRow = new HBox(12, title, spacer, sortPicker);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(headerRow, list);
        showLoading();
        loadReviews(locationId);
    }

    /**
     * Function to show a "loading" placeholder while the reviews are being fetched.
     */
    private void showLoading() {
        Label loading = new Label("Caricamento delle recensioni...");
        loading.getStyleClass().add(Styles.TEXT_MUTED);
        list.getChildren().setAll(loading);
    }

    /**
     * Function to fetch the location's reviews and the current user's liked-review ids in the
     * background.
     *
     * @param locationId the location whose reviews to fetch
     */
    private void loadReviews(String locationId) {
        Task<ReviewsData> task = new Task<>() {
            @Override
            protected ReviewsData call() throws Exception {
                var reviewService = ServiceLocator.getInstance().getReviewService();
                List<Review> reviews = reviewService.getReviewsByRestaurant(locationId);
                Set<String> likedByUser = currentUserId != null
                        ? new HashSet<>(reviewService.getUserLikedReviews(currentUserId))
                        : Set.of();
                return new ReviewsData(reviews, likedByUser);
            }
        };
        task.setOnSucceeded(e -> showReviews(task.getValue()));
        task.setOnFailed(e -> {
            LOGGER.log(Level.WARNING, "[reviews] failed to load reviews for location " + locationId, task.getException());
            Platform.runLater(() -> {
                Label error = new Label("Non è stato possibile caricare le recensioni.");
                error.getStyleClass().add(Styles.TEXT_MUTED);
                list.getChildren().setAll(error);
            });
        });

        Thread thread = new Thread(task, "reviews-section-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to store the loaded reviews and either show the "no reviews yet" placeholder or
     * reveal the sort picker and render the list.
     *
     * @param loaded the fetched reviews plus the current user's liked-review ids
     */
    private void showReviews(ReviewsData loaded) {
        this.data = loaded;
        if (loaded.reviews.isEmpty()) {
            Label placeholder = new Label("Ancora nessuna recensione per questo locale.");
            placeholder.getStyleClass().add(Styles.TEXT_MUTED);

            VBox placeholderCard = new VBox(placeholder);
            placeholderCard.getStyleClass().add("tk-card");
            placeholderCard.setPadding(new Insets(24));
            placeholderCard.setAlignment(Pos.CENTER_LEFT);

            list.getChildren().setAll(placeholderCard);
            return;
        }

        sortPicker.setVisible(true);
        sortPicker.setManaged(true);
        renderReviews();
    }

    /**
     * Function to re-sort the loaded reviews by the currently picked {@link ReviewSortOrder} and
     * rebuild the list of {@link ReviewCard}s.
     */
    private void renderReviews() {
        List<Review> sorted = new ArrayList<>(data.reviews);
        sorted.sort(sortPicker.getValue().comparator());

        list.getChildren().clear();
        for (Review review : sorted) {
            boolean liked = data.likedByUser.contains(review.getReviewId());
            list.getChildren().add(new ReviewCard(review, liked, newlyLiked -> toggleLike(review.getReviewId(), newlyLiked)));
        }
    }

    /**
     * Function to persist a like toggle on a background thread. The card's own selection state
     * has already been updated optimistically by the time this runs.
     *
     * @param reviewId the review whose like state changed
     * @param liked the review's new liked state
     */
    private void toggleLike(String reviewId, boolean liked) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                var reviewService = ServiceLocator.getInstance().getReviewService();
                if (liked) {
                    reviewService.addLikeToReview(currentUserId, reviewId);
                } else {
                    reviewService.removeLikeFromReview(currentUserId, reviewId);
                }
                return null;
            }
        };
        task.setOnFailed(e -> LOGGER.log(Level.WARNING, "[reviews] failed to toggle like on review " + reviewId,
                task.getException()));

        Thread thread = new Thread(task, "review-like-worker");
        thread.setDaemon(true);
        thread.start();
    }

    private record ReviewsData(List<Review> reviews, Set<String> likedByUser) {
    }
}
