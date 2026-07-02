package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import marin.Review;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Card showing a single {@link Review}: overall and per-aspect star ratings, the review text, the
 * like count with a toggleable like button, and the manager's reply, if any. Used by {@link
 * ReviewsSection} to render the location detail screen's review list.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class ReviewCard extends VBox {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final Button likeButton = new Button();
    private boolean liked;
    private int likes;

    /**
     * @param review the review to show
     * @param liked whether the current user has already liked this review
     * @param onToggleLike called with the new liked state when the like button is pressed
     */
    public ReviewCard(Review review, boolean liked, Consumer<Boolean> onToggleLike) {
        super(10);
        this.liked = liked;
        this.likes = review.getReviewLikes();

        Label dateLabel = new Label(review.getReviewDate() == null ? "" : review.getReviewDate().format(DATE_FORMAT));
        dateLabel.getStyleClass().add(Styles.TEXT_MUTED);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(10, new StarRating(review.getGlobalStars()), spacer, dateLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        HBox aspectsRow = new HBox(16,
                aspectLabel("Prezzo", review.getPriceStars()),
                aspectLabel("Ospitalità", review.getHospitalityStars()),
                aspectLabel("Servizio", review.getServiceStars()));
        aspectsRow.getStyleClass().add(Styles.TEXT_MUTED);
        aspectsRow.getStyleClass().add(Styles.TEXT_SMALL);

        Label textLabel = new Label(review.getText() == null || review.getText().isBlank()
                ? "(Nessun commento)" : review.getText());
        textLabel.setWrapText(true);
        if (review.getText() == null || review.getText().isBlank()) {
            textLabel.getStyleClass().add(Styles.TEXT_MUTED);
        }

        likeButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
        likeButton.setOnAction(e -> {
            this.liked = !this.liked;
            this.likes += this.liked ? 1 : -1;
            updateLikeButton();
            onToggleLike.accept(this.liked);
        });
        updateLikeButton();

        VBox card = new VBox(10, headerRow, aspectsRow, textLabel, likeButton);
        card.getStyleClass().add("tk-card");
        card.setPadding(new Insets(16));

        getChildren().add(card);

        if (review.getReply() != null) {
            getChildren().add(buildReply(review.getReply()));
        }
    }

    /**
     * Function to build one "Name: N/5" per-aspect rating label.
     *
     * @param name the aspect's display name
     * @param stars the aspect's star count out of 5
     * @return the label
     */
    private HBox aspectLabel(String name, int stars) {
        return new HBox(4, new Label(name + ":"), new Label(stars + "/5"));
    }

    /**
     * Function to redraw the like button's icon color and count to match the current
     * {@link #liked}/{@link #likes} state.
     */
    private void updateLikeButton() {
        FontIcon heart = new FontIcon(Feather.HEART);
        heart.getStyleClass().add(liked ? Styles.DANGER : Styles.TEXT_MUTED);
        likeButton.setGraphic(heart);
        likeButton.setText(String.valueOf(Math.max(0, likes)));
    }

    /**
     * Function to build the manager's reply block, indented under the review card.
     *
     * @param reply the manager's reply to render
     * @return the reply block
     */
    private VBox buildReply(marin.ReviewReply reply) {
        Label title = new Label("Risposta del gestore", new FontIcon(Feather.CORNER_DOWN_RIGHT));
        title.getStyleClass().addAll(Styles.TEXT_BOLD, Styles.TEXT_SMALL);

        Label text = new Label(reply.getText());
        text.setWrapText(true);

        VBox replyBox = new VBox(6, title, new Separator(), text);
        replyBox.getStyleClass().add("tk-card");
        replyBox.setPadding(new Insets(12, 16, 12, 16));
        VBox.setMargin(replyBox, new Insets(0, 0, 0, 24));
        return replyBox;
    }
}
