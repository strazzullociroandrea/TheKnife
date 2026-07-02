package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import marocco.Booking;
import marocco.BookingStatus;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centered modal "ticket" summarizing an existing {@link Booking}, styled like {@link
 * BookingDialog}'s summary ticket. Unlike that dialog, this one views a booking already created
 * (e.g. from the home screen's "next booking" banner, or a row in {@link
 * com.strazzullo_marocco_sibilla_marin.app.ui.BookingsView}) rather than creating a new one.
 * The "Annulla prenotazione" button only appears for bookings that can actually still be
 * cancelled (confirmed/waiting and not in the past); clicking it swaps the card to an in-place
 * confirmation step (mirroring {@link BookingDialog}'s summary-to-result step swap) rather than
 * cancelling immediately, so a misclick can't lose the booking.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class BookingDetailsDialog extends VBox {

    private static final Logger LOGGER = Logger.getLogger(BookingDetailsDialog.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ITALIAN);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * BookingDetailsDialog constructor, starting on the summary step.
     *
     * @param restaurantName the restaurant name, shown on the ticket
     * @param booking the booking to display
     * @param userId the id of the user who owns the booking, used to authorize cancellation
     * @param onClose callback invoked when the dialog should be dismissed
     * @param onCancelled callback invoked after the booking is successfully cancelled
     */
    public BookingDetailsDialog(String restaurantName, Booking booking, String userId, Runnable onClose, Runnable onCancelled) {
        getStyleClass().addAll(Styles.BG_DEFAULT, "tk-modal-card");
        setPrefWidth(380);
        setMinWidth(380);
        setMaxWidth(380);
        setMaxHeight(Region.USE_PREF_SIZE);

        showSummaryStep(restaurantName, booking, userId, onClose, onCancelled);
    }

    /**
     * Function to show the summary step: the ticket, seats/status info row, and a footer with
     * "Chiudi" plus, only if {@link #isCancellable(Booking)}, "Annulla prenotazione".
     *
     * @param restaurantName the restaurant name, shown on the ticket
     * @param booking the booking to display
     * @param userId the id of the user who owns the booking
     * @param onClose callback invoked when the dialog should be dismissed
     * @param onCancelled callback invoked after the booking is successfully cancelled
     */
    private void showSummaryStep(String restaurantName, Booking booking, String userId, Runnable onClose, Runnable onCancelled) {
        Label title = new Label("La tua prenotazione");
        title.getStyleClass().add(Styles.TITLE_4);
        Button headerCloseButton = new Button("", new FontIcon(Feather.X));
        headerCloseButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.BUTTON_CIRCLE);
        headerCloseButton.setOnAction(e -> onClose.run());
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(title, headerSpacer, headerCloseButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 16, 0, 20));

        VBox ticket = buildTicket(restaurantName, booking);
        Label seatsLabel = new Label(booking.getSeats() + (booking.getSeats() == 1 ? " persona" : " persone"));
        seatsLabel.getStyleClass().add(Styles.TEXT_BOLD);
        Label bookingStatusLabel = new Label(statusText(booking));
        bookingStatusLabel.getStyleClass().add(statusStyleClass(booking));

        Region infoSpacer = new Region();
        HBox.setHgrow(infoSpacer, Priority.ALWAYS);
        HBox infoRow = new HBox(seatsLabel, infoSpacer, bookingStatusLabel);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(20, ticket, infoRow);
        body.setPadding(new Insets(16, 20, 4, 20));

        Button closeButton = new Button("Chiudi");
        closeButton.getStyleClass().add(Styles.FLAT);
        closeButton.setOnAction(e -> onClose.run());
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 20, 20, 20));
        if (isCancellable(booking)) {
            Button cancelButton = new Button("Annulla prenotazione");
            cancelButton.getStyleClass().add(Styles.DANGER);
            cancelButton.setOnAction(e -> showConfirmStep(restaurantName, booking, userId, onClose, onCancelled));
            footer.getChildren().addAll(closeButton, footerSpacer, cancelButton);
        } else {
            footer.getChildren().addAll(footerSpacer, closeButton);
        }

        getChildren().setAll(header, body, footer);
    }

    /**
     * Function to swap the card to the in-place cancellation confirmation step: a warning
     * message and "Sì, annulla" / "Torna indietro" buttons, only the former of which actually
     * calls {@link #cancelBooking}.
     *
     * @param restaurantName the restaurant name, shown in the confirmation message
     * @param booking the booking to cancel
     * @param userId the id of the user who owns the booking
     * @param onClose callback invoked when the dialog should be dismissed
     * @param onCancelled callback invoked after the booking is successfully cancelled
     */
    private void showConfirmStep(String restaurantName, Booking booking, String userId, Runnable onClose, Runnable onCancelled) {
        Label title = new Label("Conferma annullamento");
        title.getStyleClass().add(Styles.TITLE_4);
        Button headerCloseButton = new Button("", new FontIcon(Feather.X));
        headerCloseButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.BUTTON_CIRCLE);
        headerCloseButton.setOnAction(e -> onClose.run());
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(title, headerSpacer, headerCloseButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 16, 0, 20));

        Label message = new Label("Sei sicuro di voler annullare la prenotazione da " + restaurantName + " per il "
                + Capitalization.capitalize(booking.getBookingDate().format(DATE_FORMAT)) + " alle "
                + booking.getTimeSlot().format(TIME_FORMAT) + "? L'operazione non può essere annullata.");
        message.setWrapText(true);

        MessageBanner banner = new MessageBanner();
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(16, 16);
        spinner.setVisible(false);
        Label busyLabel = new Label();
        busyLabel.getStyleClass().add(Styles.TEXT_MUTED);
        HBox busyRow = new HBox(8, spinner, busyLabel);
        busyRow.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(16, message, banner, busyRow);
        body.setPadding(new Insets(16, 20, 4, 20));

        Button backButton = new Button("Torna indietro");
        backButton.getStyleClass().add(Styles.FLAT);
        backButton.setOnAction(e -> showSummaryStep(restaurantName, booking, userId, onClose, onCancelled));
        Button confirmCancelButton = new Button("Sì, annulla");
        confirmCancelButton.getStyleClass().add(Styles.DANGER);
        confirmCancelButton.setOnAction(e -> cancelBooking(booking, userId, onCancelled,
                confirmCancelButton, backButton, spinner, busyLabel, banner));
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        HBox footer = new HBox(10, backButton, footerSpacer, confirmCancelButton);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 20, 20, 20));

        getChildren().setAll(header, body, footer);
    }

    /**
     * Function to build the "ticket": the restaurant name and, front and center, the booking's
     * date and time.
     *
     * @param restaurantName the restaurant name
     * @param booking the booking to display
     * @return the ticket
     */
    private VBox buildTicket(String restaurantName, Booking booking) {
        Label nameLabel = new Label(restaurantName);
        nameLabel.getStyleClass().add(Styles.TEXT_BOLD);

        Label timeLabel = new Label(booking.getTimeSlot().format(TIME_FORMAT));
        timeLabel.getStyleClass().add(Styles.TITLE_1);

        Label dateLabel = new Label(Capitalization.capitalize(booking.getBookingDate().format(DATE_FORMAT)));
        dateLabel.getStyleClass().add(Styles.TEXT_MUTED);

        VBox ticket = new VBox(4, nameLabel, timeLabel, dateLabel);
        ticket.setAlignment(Pos.CENTER);
        ticket.setPadding(new Insets(20));
        ticket.getStyleClass().add("tk-booking-ticket");
        return ticket;
    }

    /**
     * Function to describe a booking's status for the info row.
     *
     * @param booking the booking to describe
     * @return the Italian status description
     */
    private String statusText(Booking booking) {
        return switch (booking.getStatus()) {
            case confirmed -> "Confermata";
            case waiting -> "In lista d'attesa (posizione " + booking.getWaitingPosition() + ")";
            case cancelled -> "Annullata";
            case expired -> "Scaduta";
        };
    }

    /**
     * Function to resolve the status label's style class.
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
     * Function to check whether a booking can still be cancelled: it must be {@code confirmed} or
     * {@code waiting}, and its date/time must not already be in the past.
     *
     * @param booking the booking to check
     * @return true if the "Annulla prenotazione" button should be offered
     */
    private boolean isCancellable(Booking booking) {
        return (booking.getStatus() == BookingStatus.confirmed || booking.getStatus() == BookingStatus.waiting)
                && !LocalDateTime.of(booking.getBookingDate(), booking.getTimeSlot()).isBefore(LocalDateTime.now());
    }

    /**
     * Function to carry out the actual {@code cancelBooking} RMI call on a background thread,
     * disabling the confirm-step buttons and showing a spinner while in flight, and surfacing any
     * failure in the given banner.
     *
     * @param booking the booking to cancel
     * @param userId the id of the user who owns the booking
     * @param onCancelled callback invoked after the booking is successfully cancelled
     * @param confirmCancelButton the confirm-step's "Sì, annulla" button, disabled while in flight
     * @param backButton the confirm-step's "Torna indietro" button, disabled while in flight
     * @param spinner the confirm-step's busy spinner
     * @param busyLabel the confirm-step's busy status label
     * @param banner the confirm-step's error banner
     */
    private void cancelBooking(Booking booking, String userId, Runnable onCancelled,
                                Button confirmCancelButton, Button backButton,
                                ProgressIndicator spinner, Label busyLabel, MessageBanner banner) {
        confirmCancelButton.setDisable(true);
        backButton.setDisable(true);
        banner.hide();
        spinner.setVisible(true);
        busyLabel.setText("Annullamento in corso...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ServiceLocator.getInstance().getBookingService().cancelBooking(userId, booking.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> onCancelled.run());
        task.setOnFailed(e -> Platform.runLater(() -> {
            spinner.setVisible(false);
            busyLabel.setText("");
            banner.showError("Annullamento non riuscito: " + task.getException().getMessage());
            confirmCancelButton.setDisable(false);
            backButton.setDisable(false);
            LOGGER.log(Level.WARNING, "[booking] failed to cancel booking " + booking.getId(), task.getException());
        }));

        Thread thread = new Thread(task, "booking-cancel-worker");
        thread.setDaemon(true);
        thread.start();
    }
}
