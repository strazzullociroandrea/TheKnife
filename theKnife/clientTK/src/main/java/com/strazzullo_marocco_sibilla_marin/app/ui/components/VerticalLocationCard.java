package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.ui.AppShell;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
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
 * A premium vertical card for locations shown on the Home screen.
 * Displays a large image thumbnail at the top, followed by the restaurant details,
 * ratings, open status, and a heart button for favorites.
 *
 * @version 1.0
 */
public class VerticalLocationCard extends VBox {

    /**
     * VerticalLocationCard constructor.
     *
     * @param shell the AppShell for navigation
     * @param result the search result representing the location
     * @param onFavouriteAuthRequired callback invoked when a non-customer clicks the heart button
     */
    public VerticalLocationCard(AppShell shell, LocationSearchResult result, Runnable onFavouriteAuthRequired, Runnable onFavoriteToggled) {
        Location location = result.location();
        String restaurantName = location.getName() == null || location.getName().isBlank()
                ? result.restaurantName() : location.getName();

        getStyleClass().add("tk-card");
        setSpacing(10);
        setPadding(new Insets(12));
        setCursor(Cursor.HAND);
        setPrefWidth(240);
        setMinWidth(240);
        setMaxWidth(240);
        setOnMouseClicked(e -> shell.showLocationDetail(result));

        /** Thumbnail image */
        LocationThumbnail thumbnail = new LocationThumbnail(location.getId(), result.restaurantCuisine(), 216, 120, 12);

        /** Header containing name and heart button */
        Label nameLabel = new Label(restaurantName);
        nameLabel.getStyleClass().add(Styles.TITLE_4);
        nameLabel.setWrapText(false);
        nameLabel.setEllipsisString("...");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        ToggleButton favourite = new ToggleButton();
        favourite.setGraphic(new FontIcon(Feather.HEART));
        favourite.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "tk-favourite");

        boolean isFav = false;
        if (shell.isCustomer()) {
            strazzullo.Client client = (strazzullo.Client) shell.getCurrentUser();
            for (sibilla.Restaurant rFav : client.getFavoriteRestaurants()) {
                if (rFav.getName().equalsIgnoreCase(result.restaurantName())) {
                    isFav = true;
                    break;
                }
            }
        }
        favourite.setSelected(isFav);

        favourite.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            e.consume(); /** Prevents card selection */
            if (!shell.isCustomer()) {
                onFavouriteAuthRequired.run();
            }
        });

        favourite.setOnAction(e -> {
            if (shell.isCustomer()) {
                strazzullo.Client client = (strazzullo.Client) shell.getCurrentUser();
                if (favourite.isSelected()) {
                    boolean exists = false;
                    for (sibilla.Restaurant rFav : client.getFavoriteRestaurants()) {
                        if (rFav.getName().equalsIgnoreCase(result.restaurantName())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        sibilla.Restaurant newFav = new sibilla.Restaurant();
                        newFav.setName(result.restaurantName());
                        try {
                            newFav.setCuisine(sibilla.Cuisine.valueOf(result.restaurantCuisine().toLowerCase(Locale.ROOT)));
                        } catch (Exception ignored) {}
                        client.addFavoriteRestaurant(newFav);
                    }
                } else {
                    client.getFavoriteRestaurants().removeIf(rFav -> rFav.getName().equalsIgnoreCase(result.restaurantName()));
                }
                if (onFavoriteToggled != null) {
                    onFavoriteToggled.run();
                }
            }
        });

        HBox titleRow = new HBox(8, nameLabel, favourite);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        /** Tags row (Cuisine and Price range) */
        HBox tagsRow = new HBox(6, new TagLabel(result.restaurantCuisine()), new TagLabel(PriceLabels.of(location.getPriceRange())));
        tagsRow.setAlignment(Pos.CENTER_LEFT);

        /** Open status and rating row */
        OpenStatusPill statusPill = new OpenStatusPill(location);

        double rating = result.averageRating() == null ? 0.0 : result.averageRating();
        HBox ratingRow = new HBox(4, new StarRating(rating));
        ratingRow.setAlignment(Pos.CENTER_LEFT);

        HBox footerRow = new HBox(8, statusPill, new Region(), ratingRow);
        footerRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(footerRow.getChildren().get(1), Priority.ALWAYS);

        getChildren().addAll(thumbnail, titleRow, tagsRow, footerRow);
    }
}
