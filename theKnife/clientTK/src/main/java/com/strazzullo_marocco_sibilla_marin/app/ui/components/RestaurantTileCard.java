package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.Location;
import sibilla.LocationSearchResult;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * A vertical tile summarizing a {@link LocationSearchResult}, used by the home screen's
 * "Consigliati per te" grid, favourites row, and category-browsing rows, and by {@link
 * com.strazzullo_marocco_sibilla_marin.app.ui.FavouritesView}. Unlike {@link ResultCard}'s
 * horizontal list row, this is a compact photo-first card. In "full" mode ({@code
 * showDetails=true}) it also shows the cuisine/price tags and the open/closed status; in compact
 * mode it shows just the name and rating, matching the home screen's favourites row.
 * The 5-arg constructor is for screens only ever shown to logged-in customers, where the heart
 * is never auth-gated; the 7-arg one adds the same gate {@link ResultCard} uses, for the home
 * screen's guest-visible category rows.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class RestaurantTileCard extends VBox {

    private static final double IMAGE_WIDTH = 216;
    private static final double IMAGE_HEIGHT = 130;

    /**
     * RestaurantTileCard constructor for customer-only screens, where the heart is never
     * auth-gated.
     *
     * @param result the search result this tile represents
     * @param showDetails whether to show the cuisine/price tags and open status row
     * @param initiallyFavourite whether this location is already one of the current user's favourites
     * @param onViewDetails callback invoked when the tile is clicked
     * @param onFavouriteToggle callback invoked with the heart's new state when it is toggled
     */
    public RestaurantTileCard(LocationSearchResult result, boolean showDetails, boolean initiallyFavourite,
                               Runnable onViewDetails, Consumer<Boolean> onFavouriteToggle) {
        this(result, showDetails, initiallyFavourite, onViewDetails, onFavouriteToggle, () -> true, () -> {});
    }

    /**
     * RestaurantTileCard constructor with the heart's click auth-gated, for screens (like the
     * home screen's category rows) that may be visible to guests or managers.
     *
     * @param result the search result this tile represents
     * @param showDetails whether to show the cuisine/price tags and open status row
     * @param initiallyFavourite whether this location is already one of the current user's favourites
     * @param onViewDetails callback invoked when the tile is clicked
     * @param onFavouriteToggle callback invoked with the heart's new state when it is toggled
     * @param isCustomer supplier returning whether the current user is a logged-in customer
     * @param onFavouriteAuthRequired callback invoked when a non-customer clicks the heart button
     */
    public RestaurantTileCard(LocationSearchResult result, boolean showDetails, boolean initiallyFavourite,
                               Runnable onViewDetails, Consumer<Boolean> onFavouriteToggle,
                               BooleanSupplier isCustomer, Runnable onFavouriteAuthRequired) {
        Location location = result.location();

        getStyleClass().add("tk-card");
        setSpacing(10);
        setPadding(new Insets(12));
        setPrefWidth(IMAGE_WIDTH + 24);
        setMaxWidth(IMAGE_WIDTH + 24);
        setCursor(Cursor.HAND);
        setOnMouseClicked(e -> onViewDetails.run());

        LocationThumbnail thumbnail = new LocationThumbnail(location.getId(), result.restaurantCuisine(),
                IMAGE_WIDTH, IMAGE_HEIGHT, 16);

        ToggleButton favourite = new ToggleButton();
        favourite.setSelected(initiallyFavourite);
        favourite.setGraphic(new FontIcon(Feather.HEART));
        favourite.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.BUTTON_CIRCLE, "tk-tile-favourite", "tk-favourite");
        favourite.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (!isCustomer.getAsBoolean()) {
                e.consume();
                onFavouriteAuthRequired.run();
            }
        });
        favourite.setOnAction(e -> onFavouriteToggle.accept(favourite.isSelected()));

        StackPane imageStack = new StackPane(thumbnail, favourite);
        StackPane.setAlignment(favourite, Pos.TOP_RIGHT);
        StackPane.setMargin(favourite, new Insets(8));

        Label nameLabel = new Label(location.getName() == null || location.getName().isBlank()
                ? result.restaurantName() : location.getName());
        nameLabel.getStyleClass().add(Styles.TITLE_4);
        nameLabel.setWrapText(true);

        double rating = result.averageRating() == null ? 0.0 : result.averageRating();

        VBox content = new VBox(6, nameLabel);

        if (showDetails) {
            HBox tagsRow = new HBox(6, new TagLabel(result.restaurantCuisine()), new TagLabel(PriceLabels.of(location.getPriceRange())));
            tagsRow.setAlignment(Pos.CENTER_LEFT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox statusRow = new HBox(8, new OpenStatusPill(location), spacer, new StarRating(rating));
            statusRow.setAlignment(Pos.CENTER_LEFT);

            content.getChildren().addAll(tagsRow, statusRow);
        } else {
            content.getChildren().add(new StarRating(rating));
        }

        getChildren().addAll(imageStack, content);
    }
}
