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

import java.util.function.Consumer;

/**
 * A premium vertical card for locations shown on the Home screen.
 * Displays a large image thumbnail at the top, followed by the restaurant details,
 * ratings, open status, and a heart button for favorites. Favourite state is passed in rather
 * than read off the logged-in user, so it stays in sync with the DB-backed {@code
 * FavouriteService} that every other favourite-toggling card in the app (e.g. {@link ResultCard})
 * also goes through.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class VerticalLocationCard extends VBox {

    /**
     * VerticalLocationCard constructor.
     *
     * @param shell the AppShell for navigation
     * @param result the search result representing the location
     * @param onFavouriteAuthRequired callback invoked when a non-customer clicks the heart button
     * @param initiallyFavourite whether this location is already one of the current user's favourites
     * @param onFavouriteToggle callback invoked with the heart's new state when a customer toggles it
     */
    public VerticalLocationCard(AppShell shell, LocationSearchResult result, Runnable onFavouriteAuthRequired,
                                 boolean initiallyFavourite, Consumer<Boolean> onFavouriteToggle) {
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

        LocationThumbnail thumbnail = new LocationThumbnail(location.getId(), result.restaurantCuisine(), 216, 120, 12);

        Label nameLabel = new Label(restaurantName);
        nameLabel.getStyleClass().add(Styles.TITLE_4);
        nameLabel.setWrapText(false);
        nameLabel.setEllipsisString("...");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        ToggleButton favourite = new ToggleButton();
        favourite.setSelected(initiallyFavourite);
        favourite.setGraphic(new FontIcon(Feather.HEART));
        favourite.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "tk-favourite");
        favourite.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (!shell.isCustomer()) {
                e.consume();
                onFavouriteAuthRequired.run();
            }
        });
        favourite.setOnAction(e -> onFavouriteToggle.accept(favourite.isSelected()));

        HBox titleRow = new HBox(8, nameLabel, favourite);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        HBox tagsRow = new HBox(6, new TagLabel(result.restaurantCuisine()), new TagLabel(PriceLabels.of(location.getPriceRange())));
        tagsRow.setAlignment(Pos.CENTER_LEFT);

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
