package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.BookingDialog;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.BookingPanel;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.OpenStatusPill;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.OpeningHoursGrid;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.PhotoGallery;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.PriceLabels;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.SlotSelection;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.TagLabel;
import com.strazzullo_marocco_sibilla_marin.app.ui.map.MapPin;
import com.strazzullo_marocco_sibilla_marin.app.ui.map.MapView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.Location;
import sibilla.LocationSearchResult;

import java.util.List;
import java.util.Locale;

/**
 * Full detail screen for a single restaurant location: photo gallery, name, cuisine/dietary
 * tags, open/closed status, address, rating, price range, the full week's opening hours, a small
 * map pinpointing the location, a "Prenota un tavolo" booking panel, and (once a {@code
 * ReviewService} is exposed over RMI) its reviews. Reachable from a "Dettagli" button on every
 * {@link com.strazzullo_marocco_sibilla_marin.app.ui.components.ResultCard} in the search
 * results, and meant to be reusable from anywhere else a {@link LocationSearchResult} is
 * available, via {@link AppShell#showLocationDetail(LocationSearchResult)}.
 * Booking has no login gate yet: every booking is attributed to {@link AppShell#getCurrentUserId()}'s
 * demo placeholder until a real login screen exists.
 *
 * @version 2.0
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class LocationDetailView extends StackPane {

    private final BookingPanel bookingPanel;
    private final ModalPane modalPane = new ModalPane();

    /**
     * LocationDetailView constructor.
     *
     * @param shell the app shell, used to navigate back to the previous screen
     * @param result the location, plus its restaurant/rating info, to show
     */
    public LocationDetailView(AppShell shell, LocationSearchResult result) {
        Location location = result.location();
        String restaurantName = location.getName() == null || location.getName().isBlank()
                ? result.restaurantName() : location.getName();

        bookingPanel = new BookingPanel(location.getId(),
                selection -> openBookingDialog(restaurantName, selection, shell.getCurrentUserId()));

        VBox leftColumn = new VBox(20,
                new PhotoGallery(location.getId()),
                buildInfoCard(result, restaurantName),
                buildReviewsSection());
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        VBox rightColumn = new VBox(20, buildMapCard(location, restaurantName), bookingPanel);
        rightColumn.setPrefWidth(380);
        rightColumn.setMinWidth(320);
        rightColumn.setMaxWidth(420);

        HBox columns = new HBox(24, leftColumn, rightColumn);

        VBox content = new VBox(20, buildBreadcrumbRow(shell, location, restaurantName), columns);
        content.setPadding(new Insets(20, 32, 32, 32));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add(Styles.BG_SUBTLE);

        modalPane.setAlignment(Pos.CENTER);

        getStyleClass().add(Styles.BG_SUBTLE);
        getChildren().addAll(scroll, modalPane);
    }

    /**
     * Function to open the booking confirmation dialog as a centered modal, and refresh the
     * booking panel's slots once a booking is successfully created (seat availability may have
     * changed).
     *
     * @param restaurantName the restaurant name, shown in the dialog
     * @param selection the picked date/time to confirm
     * @param userId the id of the user making the booking
     */
    private void openBookingDialog(String restaurantName, SlotSelection selection, String userId) {
        BookingDialog dialog = new BookingDialog(restaurantName, selection, userId,
                () -> modalPane.hide(true), bookingPanel::loadSlots);
        modalPane.show(dialog);
    }

    /**
     * Function to build the top breadcrumb row: the TheKnife logo (navigates home), the
     * location's city, and the restaurant name, plus a "Indietro" button returning to wherever
     * this screen was opened from.
     *
     * @param shell the app shell, used to navigate home or back
     * @param location the location to show the breadcrumb for
     * @param restaurantName the restaurant name to show in the breadcrumb
     * @return the breadcrumb row
     */
    private HBox buildBreadcrumbRow(AppShell shell, Location location, String restaurantName) {
        Label logo = new Label("TheKnife");
        logo.getStyleClass().add(Styles.TEXT_BOLD);
        logo.setOnMouseClicked(e -> shell.showHome());
        logo.setCursor(javafx.scene.Cursor.HAND);

        HBox breadcrumb = new HBox(8,
                logo,
                new FontIcon(Feather.CHEVRON_RIGHT),
                new Label(location.getCity()),
                new FontIcon(Feather.CHEVRON_RIGHT),
                new Label(restaurantName));
        breadcrumb.setAlignment(Pos.CENTER_LEFT);
        breadcrumb.getStyleClass().add(Styles.TEXT_MUTED);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("Indietro", new FontIcon(Feather.ARROW_LEFT));
        backButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        backButton.setOnAction(e -> shell.goBack());

        HBox row = new HBox(breadcrumb, spacer, backButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Function to build the main info card: name, tags, open/closed status, address, rating,
     * price, and the full week's opening hours.
     *
     * @param result the location to show
     * @param restaurantName the restaurant name to show as the title
     * @return the info card
     */
    private VBox buildInfoCard(LocationSearchResult result, String restaurantName) {
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

        VBox card = new VBox(16, titleRow, tagsRow, metaRow, new Separator(), hoursTitle, new OpeningHoursGrid(location));
        card.getStyleClass().add("tk-card");
        card.setPadding(new Insets(24));
        return card;
    }

    /**
     * Function to build the "Dove si trova" card: a small, control-free map preview pinned on
     * the location.
     *
     * @param location the location to pin on the map
     * @param restaurantName the restaurant name, used as the pin's popup title
     * @return the map card
     */
    private VBox buildMapCard(Location location, String restaurantName) {
        Label title = new Label("Dove si trova", new FontIcon(Feather.MAP));
        title.getStyleClass().add(Styles.TITLE_3);

        MapView mapView = new MapView();
        mapView.setControlsVisible(false);
        mapView.setPrefHeight(220);
        mapView.setMinHeight(220);
        mapView.setMaxHeight(220);
        if (location.getLatitude() != null && location.getLongitude() != null) {
            mapView.setPins(List.of(new MapPin(location.getId(), location.getLatitude(), location.getLongitude(),
                    restaurantName, location.getAddress() + ", " + location.getCity())));
        }

        VBox card = new VBox(16, title, mapView);
        card.getStyleClass().add("tk-card");
        card.setPadding(new Insets(24));
        return card;
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

    /**
     * Function to build the reviews section. Shows a placeholder for now: reading reviews
     * requires a {@code ReviewService} exposed over RMI, which does not exist yet.
     *
     * @return the reviews section
     */
    private VBox buildReviewsSection() {
        Label title = new Label("Recensioni", new FontIcon(Feather.MESSAGE_CIRCLE));
        title.getStyleClass().add(Styles.TITLE_3);

        Label placeholder = new Label("Le recensioni saranno presto disponibili.");
        placeholder.getStyleClass().add(Styles.TEXT_MUTED);

        VBox placeholderCard = new VBox(placeholder);
        placeholderCard.getStyleClass().add("tk-card");
        placeholderCard.setPadding(new Insets(24));
        placeholderCard.setAlignment(Pos.CENTER_LEFT);

        return new VBox(16, title, placeholderCard);
    }
}
