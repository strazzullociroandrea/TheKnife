package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import marocco.Booking;
import marocco.BookingStatus;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.LocationSearchResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The home screen's "next booking" banner: fetches the customer's bookings in the background,
 * shows the earliest upcoming one (confirmed or waiting, not already past) styled like a green
 * ticket, and hides itself entirely when there isn't one. "Dettagli" opens {@link
 * BookingDetailsDialog}, which already includes its own cancel-confirmation step; cancelling
 * reloads this banner. Also reports every distinct location the customer has ever booked from via
 * {@code onBookedLocationsResolved}, so the host screen can boost those in its own recommendations
 * without needing to fetch the booking list itself.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class NextBookingBanner extends VBox {

    private static final DateTimeFormatter BANNER_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ITALIAN);
    private static final DateTimeFormatter BANNER_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final String userId;
    private final ModalPane modalPane;
    private final Consumer<Set<String>> onBookedLocationsResolved;

    /**
     * NextBookingBanner constructor. Starts empty and begins the background load immediately.
     *
     * @param userId the id of the logged-in customer
     * @param modalPane the host screen's modal pane, used for the booking-details dialog
     * @param onBookedLocationsResolved callback invoked with every distinct location id the
     *                                  customer has ever booked from, once the fetch completes
     */
    public NextBookingBanner(String userId, ModalPane modalPane, Consumer<Set<String>> onBookedLocationsResolved) {
        this.userId = userId;
        this.modalPane = modalPane;
        this.onBookedLocationsResolved = onBookedLocationsResolved;
        load();
    }

    /**
     * Function to reload the banner, e.g. after a booking made elsewhere may have changed which
     * one is next.
     */
    public void reload() {
        load();
    }

    /**
     * Function to fetch every booking made by the customer in the background, report the distinct
     * set of booked locations, and render the earliest upcoming one's banner, if any.
     */
    private void load() {
        Task<List<Booking>> task = new Task<>() {
            @Override
            protected List<Booking> call() throws Exception {
                return ServiceLocator.getInstance().getBookingService().listBookingsByUser(userId);
            }
        };
        task.setOnSucceeded(e -> {
            List<Booking> bookings = task.getValue();
            Set<String> bookedLocationIds = new LinkedHashSet<>();
            for (Booking booking : bookings) {
                bookedLocationIds.add(booking.getLocationId());
            }
            onBookedLocationsResolved.accept(bookedLocationIds);

            Booking next = bookings.stream()
                    .filter(b -> b.getStatus() != BookingStatus.cancelled && b.getStatus() != BookingStatus.expired)
                    .filter(b -> !LocalDateTime.of(b.getBookingDate(), b.getTimeSlot()).isBefore(LocalDateTime.now()))
                    .sorted(Comparator.comparing(Booking::getBookingDate).thenComparing(Booking::getTimeSlot))
                    .findFirst()
                    .orElse(null);

            if (next == null) {
                getChildren().clear();
            } else {
                loadRestaurantName(next);
            }
        });
        Thread thread = new Thread(task, "next-booking-banner-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to resolve the next booking's restaurant name in the background and render the
     * banner.
     *
     * @param booking the earliest upcoming booking
     */
    private void loadRestaurantName(Booking booking) {
        Task<LocationSearchResult> task = new Task<>() {
            @Override
            protected LocationSearchResult call() throws Exception {
                return ServiceLocator.getInstance().getLocationService().getLocationSearchResult(booking.getLocationId());
            }
        };
        task.setOnSucceeded(e -> render(booking, task.getValue()));
        Thread thread = new Thread(task, "next-booking-banner-name-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to render the banner, or clear it if the location couldn't be resolved.
     *
     * @param booking the earliest upcoming booking
     * @param locationResult the booking's resolved location, or null if not found
     */
    private void render(Booking booking, LocationSearchResult locationResult) {
        if (locationResult == null) {
            getChildren().clear();
            return;
        }
        String restaurantName = locationResult.location().getName() == null || locationResult.location().getName().isBlank()
                ? locationResult.restaurantName() : locationResult.location().getName();

        Label eyebrow = new Label("PROSSIMA PRENOTAZIONE");
        eyebrow.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_BOLD, Styles.SUCCESS);

        String dateText = Capitalization.capitalize(booking.getBookingDate().format(BANNER_DATE_FORMAT));
        String timeText = booking.getTimeSlot().format(BANNER_TIME_FORMAT);
        Label detailLabel = new Label(restaurantName + " · " + dateText + ", " + timeText + " · " + booking.getSeats() + " coperti");
        detailLabel.getStyleClass().add(Styles.TEXT_BOLD);

        VBox textBox = new VBox(2, eyebrow, detailLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button detailsButton = new Button("Dettagli");
        detailsButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        detailsButton.setOnAction(e -> openBookingDetails(booking, restaurantName));

        HBox banner = new HBox(16, new FontIcon(Feather.CALENDAR), textBox, spacer, detailsButton);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(16, 20, 16, 20));
        banner.getStyleClass().add("tk-next-booking-banner");

        getChildren().setAll(banner);
    }

    /**
     * Function to open the booking-details dialog as a centered modal, reloading this banner
     * once a cancellation lands.
     *
     * @param booking the booking to show
     * @param restaurantName the booking's resolved restaurant name
     */
    private void openBookingDetails(Booking booking, String restaurantName) {
        BookingDetailsDialog dialog = new BookingDetailsDialog(restaurantName, booking, userId,
                () -> modalPane.hide(true),
                () -> {
                    modalPane.hide(true);
                    load();
                });
        modalPane.show(dialog);
    }
}
