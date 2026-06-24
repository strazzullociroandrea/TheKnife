package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.Location;
import sibilla.LocationSearchResult;
import sibilla.OpeningHours;

import java.util.Locale;

/**
 * A single result row in the search results list: restaurant/location summary,
 * cuisine and price tags, open/closed status, and star rating.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class ResultCard extends VBox {

    /**
     * ResultCard constructor.
     *
     * @param result the search result this card represents
     */
    public ResultCard(LocationSearchResult result) {
        Location location = result.location();

        getStyleClass().add("tk-card");
        setSpacing(10);
        setPadding(new Insets(16));

        StackPane avatar = cuisineAvatar(result.restaurantCuisine());

        Label nameLabel = new Label(location.getName() == null || location.getName().isBlank()
                ? result.restaurantName() : location.getName());
        nameLabel.getStyleClass().add(Styles.TITLE_4);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = buildStatusLabel(location);

        ToggleButton favourite = new ToggleButton();
        favourite.setGraphic(new FontIcon(Feather.HEART));
        favourite.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "tk-favourite");

        HBox headerRow = new HBox(12, avatar, nameLabel, spacer, statusLabel, favourite);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        HBox tagsRow = new HBox(6, tag(result.restaurantCuisine()), tag(priceLabel(location.getPriceRange())));
        tagsRow.setAlignment(Pos.CENTER_LEFT);

        HBox addressRow = new HBox(6,
                new FontIcon(Feather.MAP_PIN),
                new Label(location.getAddress() + ", " + location.getCity()));
        addressRow.setAlignment(Pos.CENTER_LEFT);
        addressRow.getStyleClass().add(Styles.TEXT_MUTED);

        HBox ratingRow = buildRatingRow(result);

        getChildren().addAll(headerRow, tagsRow, addressRow, ratingRow);
    }

    /**
     * Function to build a small circular cuisine icon avatar, echoing the same circular badge
     * language used by the quick-filter cuisine chips and by the simplified map pins, so the
     * list and the map read as one consistent design rather than two clashing styles.
     *
     * @param cuisine the restaurant's cuisine type, matching a {@code /icons/cuisine/<cuisine>.png} asset
     * @return the avatar, or an empty circle if the icon asset is missing
     */
    private StackPane cuisineAvatar(String cuisine) {
        StackPane circle = new StackPane();
        circle.getStyleClass().add("tk-card-avatar");
        circle.setPrefSize(36, 36);
        circle.setMinSize(36, 36);
        circle.setMaxSize(36, 36);

        var stream = cuisine == null ? null
                : getClass().getResourceAsStream("/icons/cuisine/" + cuisine.toLowerCase(Locale.ROOT) + ".png");
        if (stream != null) {
            ImageView icon = new ImageView(new Image(stream));
            icon.setFitWidth(20);
            icon.setFitHeight(20);
            icon.setPreserveRatio(true);
            icon.setSmooth(true);
            circle.getChildren().add(icon);
        }
        return circle;
    }

    /**
     * Function to build the open/closed status pill as a small colored dot plus label, a
     * lighter-weight treatment than a solid color block.
     *
     * @param location the location whose opening hours determine the status
     * @return the status pill
     */
    private Label buildStatusLabel(Location location) {
        boolean open = OpeningHours.isOpenNow(location);
        FontIcon dot = new FontIcon(Feather.CIRCLE);
        dot.getStyleClass().add(open ? Styles.SUCCESS : Styles.DANGER);
        Label statusLabel = new Label(open ? "Aperto" : "Chiuso", dot);
        statusLabel.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_BOLD, "tk-status-pill",
                open ? "tk-status-open" : "tk-status-closed");
        return statusLabel;
    }

    /**
     * Function to build a small rounded tag label.
     *
     * @param text the tag text
     * @return the tag label
     */
    private Label tag(String text) {
        Label label = new Label(text == null ? "" : capitalize(text));
        label.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_BOLD, "tk-tag");
        label.setPadding(new Insets(3, 10, 3, 10));
        return label;
    }

    /**
     * Function to capitalize the first letter of a tag's text.
     *
     * @param text the text to capitalize
     * @return the capitalized text
     */
    private String capitalize(String text) {
        return text.isEmpty() ? text : text.substring(0, 1).toUpperCase(Locale.ITALIAN) + text.substring(1);
    }

    /**
     * Function to render a location's price range as a euro-sign scale.
     *
     * @param priceRange the location's price range
     * @return a string of one to three euro signs
     */
    private String priceLabel(int priceRange) {
        if (priceRange <= 15) return "€";
        if (priceRange <= 35) return "€€";
        return "€€€";
    }

    /**
     * Function to build the star rating row for a result.
     *
     * @param result the search result
     * @return the rating row
     */
    private HBox buildRatingRow(LocationSearchResult result) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        double rating = result.averageRating() == null ? 0.0 : result.averageRating();
        int fullStars = Math.round((float) rating);
        for (int i = 1; i <= 5; i++) {
            FontIcon star = new FontIcon(Feather.STAR);
            star.getStyleClass().add(i <= fullStars ? Styles.WARNING : Styles.TEXT_MUTED);
            row.getChildren().add(star);
        }
        Label reviewCount = new Label(result.averageRating() == null
                ? "Nessuna recensione"
                : String.format(Locale.ITALIAN, "%.1f (%d recensioni)", rating, result.reviewCount()));
        reviewCount.getStyleClass().add(Styles.TEXT_MUTED);
        row.getChildren().add(reviewCount);
        return row;
    }
}
