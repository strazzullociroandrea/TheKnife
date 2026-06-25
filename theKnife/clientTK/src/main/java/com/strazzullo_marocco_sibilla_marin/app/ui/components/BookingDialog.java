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
import javafx.scene.text.TextAlignment;
import marocco.Booking;
import marocco.BookingStatus;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centered modal "ticket" summarizing a {@link SlotSelection}: restaurant, date and time front
 * and center, and a number-of-people stepper, since that's the one thing {@link BookingPanel}
 * doesn't already ask for. Confirming carries out the actual {@code BookingService.createBooking}
 * call and swaps the ticket for its outcome (confirmed, or queued on the waiting list) in place.
 *
 * @version 2.0
 * @Author Marocco Stefano, 762192, VA - author of this revision
 */
public class BookingDialog extends VBox {

    private static final Logger LOGGER = Logger.getLogger(BookingDialog.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ITALIAN);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MIN_PEOPLE = 1;
    private static final int MAX_PEOPLE = 20;
    private static final int DEFAULT_PEOPLE = 2;

    private final SlotSelection selection;
    private final String userId;
    private final Runnable onClose;
    private final Runnable onBookingCompleted;

    private final IntegerStepper peopleStepper = new IntegerStepper(MIN_PEOPLE, MAX_PEOPLE, DEFAULT_PEOPLE);
    private final Label statusLabel = new Label();
    private final ProgressIndicator spinner = new ProgressIndicator();
    private final Button cancelButton = new Button("Annulla");
    private final Button confirmButton = new Button("Conferma prenotazione");

    /**
     * BookingDialog constructor, starting on the summary step.
     *
     * @param restaurantName the restaurant name, shown on the ticket
     * @param selection the picked date/time to confirm
     * @param userId the id of the user making the booking
     * @param onClose callback invoked when the dialog should be dismissed
     * @param onBookingCompleted callback invoked after a booking was successfully created,
     *                           e.g. to refresh the host screen's slot availability
     */
    public BookingDialog(String restaurantName, SlotSelection selection, String userId,
                          Runnable onClose, Runnable onBookingCompleted) {
        this.selection = selection;
        this.userId = userId;
        this.onClose = onClose;
        this.onBookingCompleted = onBookingCompleted;

        getStyleClass().addAll(Styles.BG_DEFAULT, "tk-modal-card");
        setPrefWidth(380);
        setMinWidth(380);
        setMaxWidth(380);
        setMaxHeight(Region.USE_PREF_SIZE);

        showSummaryStep(restaurantName);
    }

    private void showSummaryStep(String restaurantName) {
        Label title = new Label("La tua prenotazione");
        title.getStyleClass().add(Styles.TITLE_4);
        Button closeButton = new Button("", new FontIcon(Feather.X));
        closeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.BUTTON_CIRCLE);
        closeButton.setOnAction(e -> onClose.run());
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(title, headerSpacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 16, 0, 20));

        VBox ticket = buildTicket(restaurantName);
        VBox peopleSection = buildPeopleStepper();

        statusLabel.getStyleClass().add(Styles.TEXT_MUTED);
        statusLabel.setWrapText(true);
        spinner.setMaxSize(16, 16);
        spinner.setVisible(false);
        HBox statusRow = new HBox(8, spinner, statusLabel);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(20, ticket, peopleSection, statusRow);
        body.setPadding(new Insets(16, 20, 4, 20));

        cancelButton.getStyleClass().add(Styles.FLAT);
        cancelButton.setOnAction(e -> onClose.run());
        confirmButton.getStyleClass().add(Styles.ACCENT);
        confirmButton.setOnAction(e -> confirmBooking());
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        HBox footer = new HBox(10, cancelButton, footerSpacer, confirmButton);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 20, 20, 20));

        getChildren().setAll(header, body, footer);
    }

    /**
     * Function to build the "ticket": the restaurant name and, front and center, the date and
     * time the booking is for.
     *
     * @param restaurantName the restaurant name
     * @return the ticket
     */
    private VBox buildTicket(String restaurantName) {
        Label nameLabel = new Label(restaurantName);
        nameLabel.getStyleClass().add(Styles.TEXT_BOLD);

        Label timeLabel = new Label(selection.timeSlot().format(TIME_FORMAT));
        timeLabel.getStyleClass().add(Styles.TITLE_1);

        Label dateLabel = new Label(Capitalization.capitalize(selection.date().format(DATE_FORMAT)));
        dateLabel.getStyleClass().add(Styles.TEXT_MUTED);

        VBox ticket = new VBox(4, nameLabel, timeLabel, dateLabel);
        ticket.setAlignment(Pos.CENTER);
        ticket.setPadding(new Insets(20));
        ticket.getStyleClass().add("tk-booking-ticket");
        return ticket;
    }

    /**
     * Function to build the "Numero di persone" stepper section.
     *
     * @return the people stepper section
     */
    private VBox buildPeopleStepper() {
        Label peopleTitle = new Label("Numero di persone");
        peopleTitle.getStyleClass().add(Styles.TEXT_BOLD);

        VBox section = new VBox(10, peopleTitle, peopleStepper);
        section.setAlignment(Pos.CENTER_LEFT);
        return section;
    }

    private void confirmBooking() {
        confirmButton.setDisable(true);
        cancelButton.setDisable(true);
        spinner.setVisible(true);
        statusLabel.setText("Invio della prenotazione in corso...");

        Task<Booking> task = new Task<>() {
            @Override
            protected Booking call() throws Exception {
                return ServiceLocator.getInstance().getBookingService().createBooking(
                        userId, selection.locationId(), selection.date(), selection.timeSlot(), peopleStepper.getValue());
            }
        };
        task.setOnSucceeded(e -> {
            onBookingCompleted.run();
            showResultStep(task.getValue());
        });
        task.setOnFailed(e -> Platform.runLater(() -> {
            spinner.setVisible(false);
            statusLabel.setText("Prenotazione non riuscita: " + task.getException().getMessage());
            confirmButton.setDisable(false);
            cancelButton.setDisable(false);
            LOGGER.log(Level.WARNING, "[booking] failed to create booking for location " + selection.locationId(),
                    task.getException());
        }));

        Thread thread = new Thread(task, "booking-create-worker");
        thread.setDaemon(true);
        thread.start();
    }

    private void showResultStep(Booking booking) {
        boolean confirmed = booking.getStatus() == BookingStatus.confirmed;

        FontIcon resultIcon = new FontIcon(confirmed ? Feather.CHECK_CIRCLE : Feather.CLOCK);
        resultIcon.getStyleClass().add(confirmed ? Styles.SUCCESS : Styles.WARNING);
        resultIcon.setIconSize(40);

        Label resultTitle = new Label(confirmed ? "Prenotazione confermata" : "Prenotazione in lista d'attesa");
        resultTitle.getStyleClass().add(Styles.TITLE_3);

        Label resultDetail = new Label(confirmed
                ? "Il tavolo è confermato per le " + booking.getTimeSlot().format(TIME_FORMAT)
                        + " del " + Capitalization.capitalize(booking.getBookingDate().format(DATE_FORMAT)) + "."
                : "Il locale è al completo per questo orario: sei in posizione "
                        + booking.getWaitingPosition() + " in lista d'attesa.");
        resultDetail.setWrapText(true);
        resultDetail.getStyleClass().add(Styles.TEXT_MUTED);
        resultDetail.setTextAlignment(TextAlignment.CENTER);

        Button closeButton = new Button("Chiudi");
        closeButton.getStyleClass().add(Styles.ACCENT);
        closeButton.setMaxWidth(Double.MAX_VALUE);
        closeButton.setOnAction(e -> onClose.run());

        VBox body = new VBox(12, resultIcon, resultTitle, resultDetail, closeButton);
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(32, 24, 28, 24));

        getChildren().setAll(body);
    }
}
