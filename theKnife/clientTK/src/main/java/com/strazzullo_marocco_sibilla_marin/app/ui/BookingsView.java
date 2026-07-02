package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.BookingDetailsDialog;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.Capitalization;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.EmptyState;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import marocco.Booking;
import marocco.BookingStatus;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.LocationSearchResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Screen listing every booking a logged-in customer has made, split into "Prossime prenotazioni"
 * (confirmed/waiting and not yet past, soonest first) and "Prenotazioni passate" (everything
 * else: past, cancelled, or expired, most recent first). Reachable from {@link
 * com.strazzullo_marocco_sibilla_marin.app.ui.components.HomeToolbar}'s "Prenotazioni" button via
 * {@link AppShell#showBookings()}. Each row's "Dettagli" opens the same {@link
 * BookingDetailsDialog} the home screen's "next booking" banner uses; cancelling one reloads the
 * whole list.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class BookingsView extends StackPane {

    private static final DateTimeFormatter ROW_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ITALIAN);
    private static final DateTimeFormatter ROW_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppShell shell;
    private final ModalPane modalPane = new ModalPane();
    private final VBox upcomingList = new VBox(12);
    private final VBox pastList = new VBox(12);
    private final VBox upcomingSection;
    private final VBox pastSection;
    private final EmptyState emptyState;

    /**
     * BookingsView constructor. Builds the screen and loads the bookings.
     *
     * @param shell the app shell, used to navigate back and to show a booking's location detail
     */
    public BookingsView(AppShell shell) {
        this.shell = shell;

        Label upcomingTitle = new Label("Prossime prenotazioni");
        upcomingTitle.getStyleClass().add(Styles.TITLE_3);
        upcomingSection = new VBox(12, upcomingTitle, upcomingList);
        upcomingSection.setVisible(false);
        upcomingSection.setManaged(false);

        Label pastTitle = new Label("Prenotazioni passate");
        pastTitle.getStyleClass().add(Styles.TITLE_3);
        pastSection = new VBox(12, pastTitle, pastList);
        pastSection.setVisible(false);
        pastSection.setManaged(false);

        emptyState = new EmptyState(Feather.CALENDAR, "Nessuna prenotazione",
                "Non hai ancora prenotato nessun tavolo. Trova un ristorante e prenota il tuo primo posto.",
                "Cerca un ristorante", () -> shell.showSearch(""));
        emptyState.setVisible(false);
        emptyState.setManaged(false);

        VBox content = new VBox(20, buildBreadcrumbRow(), buildTitle(), upcomingSection, pastSection, emptyState);
        content.setPadding(new Insets(20, 32, 32, 32));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add(Styles.BG_SUBTLE);

        modalPane.setAlignment(Pos.CENTER);
        getStyleClass().add(Styles.BG_SUBTLE);
        getChildren().addAll(scroll, modalPane);

        loadBookings();
    }

    /**
     * Function to build the "Le mie prenotazioni" title.
     *
     * @return the title label
     */
    private Label buildTitle() {
        Label title = new Label("Le mie prenotazioni", new FontIcon(Feather.CALENDAR));
        title.getStyleClass().add(Styles.TITLE_2);
        return title;
    }

    /**
     * Function to build the top breadcrumb row: the TheKnife logo (navigates home) and an
     * "Indietro" button returning to wherever this screen was opened from.
     *
     * @return the breadcrumb row
     */
    private HBox buildBreadcrumbRow() {
        Label logo = new Label("TheKnife");
        logo.getStyleClass().add(Styles.TEXT_BOLD);
        logo.setOnMouseClicked(e -> shell.showHome());
        logo.setCursor(Cursor.HAND);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("Indietro", new FontIcon(Feather.ARROW_LEFT));
        backButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        backButton.setOnAction(e -> shell.goBack());

        HBox row = new HBox(logo, spacer, backButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Function to fetch every booking made by the user in the background.
     */
    private void loadBookings() {
        String userId = shell.getCurrentUserId();
        Task<List<Booking>> task = new Task<>() {
            @Override
            protected List<Booking> call() throws Exception {
                return ServiceLocator.getInstance().getBookingService().listBookingsByUser(userId);
            }
        };
        task.setOnSucceeded(e -> resolveRestaurantNames(task.getValue()));
        Thread thread = new Thread(task, "bookings-load-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to resolve each distinct location's restaurant name in the background (one RMI
     * call per distinct location, not per booking), then render.
     *
     * @param bookings the user's bookings
     */
    private void resolveRestaurantNames(List<Booking> bookings) {
        Task<Map<String, String>> task = new Task<>() {
            @Override
            protected Map<String, String> call() throws Exception {
                Map<String, String> names = new HashMap<>();
                for (Booking booking : bookings) {
                    names.computeIfAbsent(booking.getLocationId(), this::resolveName);
                }
                return names;
            }

            /**
             * Function to resolve a single location's restaurant name, falling back to a
             * generic label if the location can't be found or the lookup fails.
             *
             * @param locationId the location id to resolve
             * @return the resolved restaurant name
             */
            private String resolveName(String locationId) {
                try {
                    LocationSearchResult result = ServiceLocator.getInstance().getLocationService().getLocationSearchResult(locationId);
                    if (result == null) {
                        return "Ristorante";
                    }
                    String name = result.location().getName();
                    return name == null || name.isBlank() ? result.restaurantName() : name;
                } catch (Exception e) {
                    return "Ristorante";
                }
            }
        };
        task.setOnSucceeded(e -> render(bookings, task.getValue()));
        Thread thread = new Thread(task, "bookings-names-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to split bookings into "upcoming" and "past" sections and render each as a row.
     *
     * @param bookings the user's bookings
     * @param restaurantNames the resolved restaurant name for each distinct location id
     */
    private void render(List<Booking> bookings, Map<String, String> restaurantNames) {
        LocalDateTime now = LocalDateTime.now();

        List<Booking> upcoming = bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.cancelled && b.getStatus() != BookingStatus.expired)
                .filter(b -> !LocalDateTime.of(b.getBookingDate(), b.getTimeSlot()).isBefore(now))
                .sorted(Comparator.comparing(Booking::getBookingDate).thenComparing(Booking::getTimeSlot))
                .toList();
        List<Booking> past = bookings.stream()
                .filter(b -> !upcoming.contains(b))
                .sorted(Comparator.comparing(Booking::getBookingDate).thenComparing(Booking::getTimeSlot).reversed())
                .toList();

        upcomingList.getChildren().clear();
        for (Booking booking : upcoming) {
            upcomingList.getChildren().add(buildRow(booking, restaurantNames.get(booking.getLocationId())));
        }
        upcomingSection.setVisible(!upcoming.isEmpty());
        upcomingSection.setManaged(!upcoming.isEmpty());

        pastList.getChildren().clear();
        for (Booking booking : past) {
            pastList.getChildren().add(buildRow(booking, restaurantNames.get(booking.getLocationId())));
        }
        pastSection.setVisible(!past.isEmpty());
        pastSection.setManaged(!past.isEmpty());

        boolean empty = bookings.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
    }

    /**
     * Function to build a single booking row: restaurant name, date/time/seats, a status pill,
     * and a "Dettagli" button opening {@link BookingDetailsDialog}.
     *
     * @param booking the booking to render
     * @param restaurantName the booking's resolved restaurant name
     * @return the row
     */
    private HBox buildRow(Booking booking, String restaurantName) {
        Label nameLabel = new Label(restaurantName);
        nameLabel.getStyleClass().add(Styles.TEXT_BOLD);

        String dateText = Capitalization.capitalize(booking.getBookingDate().format(ROW_DATE_FORMAT));
        Label detailLabel = new Label(dateText + " · " + booking.getTimeSlot().format(ROW_TIME_FORMAT) + " · "
                + booking.getSeats() + (booking.getSeats() == 1 ? " persona" : " persone"));
        detailLabel.getStyleClass().add(Styles.TEXT_MUTED);

        VBox textBox = new VBox(4, nameLabel, detailLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label statusPill = new Label(statusText(booking));
        statusPill.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_BOLD, statusStyleClass(booking));

        Button detailsButton = new Button("Dettagli");
        detailsButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        detailsButton.setOnAction(e -> openDetails(booking, restaurantName));

        HBox row = new HBox(16, new FontIcon(Feather.CALENDAR), textBox, statusPill, detailsButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("tk-card");
        row.setPadding(new Insets(16));
        return row;
    }

    /**
     * Function to describe a booking's status for its row's status pill.
     *
     * @param booking the booking to describe
     * @return the Italian status description
     */
    private String statusText(Booking booking) {
        return switch (booking.getStatus()) {
            case confirmed -> "Confermata";
            case waiting -> "In attesa";
            case cancelled -> "Annullata";
            case expired -> "Scaduta";
        };
    }

    /**
     * Function to resolve a booking's status pill style class.
     *
     * @param booking the booking to describe
     * @return the AtlantaFX semantic style class matching the status
     */
    private String statusStyleClass(Booking booking) {
        return switch (booking.getStatus()) {
            case confirmed -> Styles.SUCCESS;
            case waiting -> Styles.WARNING;
            case cancelled, expired -> Styles.DANGER;
        };
    }

    /**
     * Function to open the booking-details dialog as a centered modal, reloading the whole list
     * once a cancellation lands.
     *
     * @param booking the booking to show
     * @param restaurantName the booking's resolved restaurant name
     */
    private void openDetails(Booking booking, String restaurantName) {
        BookingDetailsDialog dialog = new BookingDetailsDialog(restaurantName, booking, shell.getCurrentUserId(),
                () -> modalPane.hide(true),
                () -> {
                    modalPane.hide(true);
                    loadBookings();
                });
        modalPane.show(dialog);
    }
}
