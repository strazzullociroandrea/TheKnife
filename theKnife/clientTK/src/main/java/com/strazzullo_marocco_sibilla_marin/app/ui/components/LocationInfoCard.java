package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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
 * The location detail screen's main info card: name, cuisine/dietary tags, open/closed status,
 * address, rating, price range, and the full week's opening hours.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class LocationInfoCard extends VBox {

    /**
     * @param result the location to show
     * @param restaurantName the restaurant name to show as the title
     */
    public LocationInfoCard(LocationSearchResult result, String restaurantName) {
        super(16);
        Location location = result.location();

        Label nameLabel = new Label(restaurantName);
        nameLabel.getStyleClass().add(Styles.TITLE_2);

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        HBox titleRow = new HBox(12, nameLabel, titleSpacer, new OpenStatusPill(location));
        titleRow.setAlignment(Pos.CENTER_LEFT);

        HBox tagsRow = new HBox(6);
        tagsRow.setAlignment(Pos.CENTER_LEFT);
        tagsRow.getChildren().add(new TagLabel(result.restaurantCuisine()));
        if (location.isVegetarianMenu()) {
            tagsRow.getChildren().add(new TagLabel("Vegetariano"));
        }
        if (location.isVeganMenu()) {
            tagsRow.getChildren().add(new TagLabel("Vegano"));
        }
        if (location.isGlutenFreeMenu()) {
            tagsRow.getChildren().add(new TagLabel("Senza glutine"));
        }

        HBox metaRow = new HBox(16,
                metaItem(Feather.MAP_PIN, location.getAddress() + ", " + location.getCity()),
                metaItem(Feather.STAR, ratingText(result)),
                metaItem(null, PriceLabels.of(location.getPriceRange())));
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.getStyleClass().add(Styles.TEXT_MUTED);

        Label hoursTitle = new Label("Orari", new FontIcon(Feather.CLOCK));
        hoursTitle.getStyleClass().add(Styles.TITLE_3);

        getChildren().addAll(titleRow, tagsRow, metaRow, new Separator(), hoursTitle, new OpeningHoursGrid(location));
        getStyleClass().add("tk-card");
        setPadding(new Insets(24));
    }

    /**
     * Function to build a small icon-plus-text item for the address/rating/price meta row.
     *
     * @param icon the leading icon, or null for none
     * @param text the item text
     * @return the meta item
     */
    private HBox metaItem(Feather icon, String text) {
        HBox item = new HBox(6, new Label(text));
        if (icon != null) {
            item.getChildren().add(0, new FontIcon(icon));
        }
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    /**
     * Function to format a result's rating and review count for the meta row.
     *
     * @param result the search result to format
     * @return the formatted rating text
     */
    private String ratingText(LocationSearchResult result) {
        if (result.averageRating() == null) {
            return "Nessuna recensione";
        }
        return String.format(Locale.ITALIAN, "%.1f (%d recensioni)", result.averageRating(), result.reviewCount());
    }
}
