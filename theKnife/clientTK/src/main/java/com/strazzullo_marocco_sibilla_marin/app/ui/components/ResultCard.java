package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.Location;
import sibilla.LocationSearchResult;

import java.util.Locale;

/**
 * A single result row in the search results list: restaurant/location summary, cuisine and
 * price tags, open/closed status, star rating, and a photo thumbnail. The whole card is
 * clickable, opening the full {@link
 * com.strazzullo_marocco_sibilla_marin.app.ui.LocationDetailView} for this location, with a
 * trailing chevron hinting at it.
 *
 * @version 3.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class ResultCard extends VBox {

    /**
     * ResultCard constructor.
     *
     * @param result the search result this card represents
     * @param onViewDetails callback invoked when the card is clicked
     */
    public ResultCard(LocationSearchResult result, Runnable onViewDetails) {
        Location location = result.location();

        getStyleClass().add("tk-card");
        setSpacing(10);
        setPadding(new Insets(16));
        setCursor(Cursor.HAND);
        setOnMouseClicked(e -> onViewDetails.run());

        Label nameLabel = new Label(location.getName() == null || location.getName().isBlank()
                ? result.restaurantName() : location.getName());
        nameLabel.getStyleClass().add(Styles.TITLE_4);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        OpenStatusPill statusPill = new OpenStatusPill(location);

        ToggleButton favourite = new ToggleButton();
        favourite.setGraphic(new FontIcon(Feather.HEART));
        favourite.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "tk-favourite");
        favourite.setOnMouseClicked(Event::consume);

        HBox headerRow = new HBox(12, nameLabel, spacer, statusPill, favourite);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        HBox tagsRow = new HBox(6, new TagLabel(result.restaurantCuisine()), new TagLabel(PriceLabels.of(location.getPriceRange())));
        tagsRow.setAlignment(Pos.CENTER_LEFT);

        HBox addressRow = new HBox(6,
                new FontIcon(Feather.MAP_PIN),
                new Label(location.getAddress() + ", " + location.getCity()));
        addressRow.setAlignment(Pos.CENTER_LEFT);
        addressRow.getStyleClass().add(Styles.TEXT_MUTED);

        HBox ratingRow = buildRatingRow(result);

        VBox content = new VBox(8, headerRow, tagsRow, addressRow, ratingRow);
        HBox.setHgrow(content, Priority.ALWAYS);

        FontIcon chevron = new FontIcon(Feather.CHEVRON_RIGHT);
        chevron.getStyleClass().add(Styles.TEXT_MUTED);

        HBox topRow = new HBox(16, new LocationThumbnail(location.getId(), result.restaurantCuisine()), content, chevron);
        topRow.setAlignment(Pos.CENTER_LEFT);

        getChildren().add(topRow);
    }

    /**
     * Function to build the star rating row for a result.
     *
     * @param result the search result
     * @return the rating row
     */
    private HBox buildRatingRow(LocationSearchResult result) {
        double rating = result.averageRating() == null ? 0.0 : result.averageRating();
        Label reviewCount = new Label(result.averageRating() == null
                ? "Nessuna recensione"
                : String.format(Locale.ITALIAN, "%.1f (%d recensioni)", rating, result.reviewCount()));
        reviewCount.getStyleClass().add(Styles.TEXT_MUTED);

        HBox row = new HBox(4, new StarRating(rating), reviewCount);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
