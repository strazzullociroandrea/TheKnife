package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.BookingDialog;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.BookingPanel;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.LocationInfoCard;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.PhotoGallery;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.ReviewsSection;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.SlotSelection;
import com.strazzullo_marocco_sibilla_marin.app.ui.map.MapPin;
import com.strazzullo_marocco_sibilla_marin.app.ui.map.MapView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

/**
 * Full detail screen for a single restaurant location: photo gallery, name, cuisine/dietary
 * tags, open/closed status, address, rating, price range, the full week's opening hours, a small
 * map pinpointing the location, a booking section, and its reviews. Reachable from a "Dettagli"
 * button on every {@link com.strazzullo_marocco_sibilla_marin.app.ui.components.ResultCard} in
 * the search results, and meant to be reusable from anywhere else a {@link LocationSearchResult}
 * is available, via {@link AppShell#showLocationDetail(LocationSearchResult)}.
 * The booking section is role-aware: customers see the full {@link
 * com.strazzullo_marocco_sibilla_marin.app.ui.components.BookingPanel}, managers see a
 * restriction notice, and guests see a prompt with links to log in or register.
 *
 * @version 5.0
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class LocationDetailView extends StackPane {

    private BookingPanel bookingPanel;
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

        Node bookingSection;
        if (shell.isCustomer()) {
            bookingPanel = new BookingPanel(location.getId(),
                    selection -> openBookingDialog(restaurantName, selection, shell.getCurrentUserId()));
            bookingSection = bookingPanel;
        } else if (shell.isLoggedIn()) {
            bookingSection = buildManagerRestrictionCard();
        } else {
            bookingSection = buildBookingLoginPrompt(shell);
        }

        VBox leftColumn = new VBox(20,
                new PhotoGallery(location.getId()),
                new LocationInfoCard(result, restaurantName),
                new ReviewsSection(location.getId(), shell.getCurrentUserId()));
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        VBox rightColumn = new VBox(20, buildMapCard(location, restaurantName), bookingSection);
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
     * Function to build the booking placeholder shown when a manager is logged in, informing them
     * that bookings are reserved for customer accounts.
     *
     * @return the customer-only restriction card
     */
    private VBox buildManagerRestrictionCard() {
        Label title = new Label("Prenota un tavolo", new FontIcon(Feather.CALENDAR));
        title.getStyleClass().add(Styles.TITLE_3);

        Label message = new Label("Le prenotazioni sono disponibili solo per i clienti.");
        message.getStyleClass().add(Styles.TEXT_MUTED);
        message.setWrapText(true);

        VBox card = new VBox(16, title, message);
        card.getStyleClass().add("tk-card");
        card.setPadding(new Insets(24));
        return card;
    }

    /**
     * Function to build the "Prenota un tavolo" placeholder shown when the user is not logged in:
     * a short message and two buttons leading to the login and registration screens.
     *
     * @param shell the app shell, used to navigate to login or registration
     * @return the login-required booking card
     */
    private VBox buildBookingLoginPrompt(AppShell shell) {
        Label title = new Label("Prenota un tavolo", new FontIcon(Feather.CALENDAR));
        title.getStyleClass().add(Styles.TITLE_3);

        Label message = new Label("Per prenotare accedi o registrati.");
        message.getStyleClass().add(Styles.TEXT_MUTED);
        message.setWrapText(true);

        Button loginButton = new Button("Accedi");
        loginButton.getStyleClass().add(Styles.ACCENT);
        loginButton.setOnAction(e -> shell.showLogin());

        Button registerButton = new Button("Registrati");
        registerButton.setOnAction(e -> shell.showRegistrationView());

        HBox buttons = new HBox(8, loginButton, registerButton);

        VBox card = new VBox(16, title, message, buttons);
        card.getStyleClass().add("tk-card");
        card.setPadding(new Insets(24));
        return card;
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
}
