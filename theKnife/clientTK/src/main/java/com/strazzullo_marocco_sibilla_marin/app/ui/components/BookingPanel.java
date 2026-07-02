package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * "Prenota un tavolo" card: a date picker and the location's available time slots for that date
 * (from {@code BookingService.getAvailableTimeSlots}), ending in a callback with the picked
 * {@link SlotSelection} once "Prenota" is pressed. How many people the booking is for is asked
 * next, in {@link BookingDialog} — this panel is only about *when*, not *how many*.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class BookingPanel extends VBox {

    private static final Logger LOGGER = Logger.getLogger(BookingPanel.class.getName());

    private final String locationId;
    private final DateSelector dateSelector = new DateSelector(LocalDate.now());
    private final FlowPane slotsPane = new FlowPane(8, 8);
    private final ToggleGroup slotGroup = new ToggleGroup();
    private final Label statusLabel = new Label();
    private final Button bookButton = new Button("Prenota");

    /**
     * BookingPanel constructor. Starts loading the available slots for today immediately.
     *
     * @param locationId the location to book
     * @param onSlotPicked callback invoked with the picked date/time when "Prenota" is pressed
     */
    public BookingPanel(String locationId, Consumer<SlotSelection> onSlotPicked) {
        this.locationId = locationId;
        setSpacing(16);

        Label title = new Label("Prenota un tavolo", new FontIcon(Feather.CALENDAR));
        title.getStyleClass().add(Styles.TITLE_3);

        dateSelector.valueProperty().addListener((obs, oldDate, newDate) -> loadSlots());

        Label dateLabel = new Label("Data");
        dateLabel.getStyleClass().add(Styles.TEXT_BOLD);

        Label slotsLabel = new Label("Orario");
        slotsLabel.getStyleClass().add(Styles.TEXT_BOLD);

        statusLabel.getStyleClass().add(Styles.TEXT_MUTED);
        statusLabel.setWrapText(true);

        bookButton.getStyleClass().add(Styles.ACCENT);
        bookButton.setMaxWidth(Double.MAX_VALUE);
        bookButton.setDisable(true);
        bookButton.setOnAction(e -> {
            ToggleButton selected = (ToggleButton) slotGroup.getSelectedToggle();
            if (selected != null) {
                onSlotPicked.accept(new SlotSelection(locationId, dateSelector.getValue(), (LocalTime) selected.getUserData()));
            }
        });

        getChildren().addAll(title, dateLabel, dateSelector, slotsLabel, slotsPane, statusLabel, bookButton);
        getStyleClass().add("tk-card");
        setPadding(new Insets(24));

        loadSlots();
    }

    /**
     * Function to reload the available time slots for the currently selected date, e.g. after a
     * booking was just made and seat availability may have changed.
     */
    public void loadSlots() {
        slotsPane.getChildren().clear();
        slotGroup.getToggles().clear();
        bookButton.setDisable(true);
        statusLabel.setText("Caricamento orari...");

        LocalDate date = dateSelector.getValue();
        Task<List<LocalTime>> task = new Task<>() {
            @Override
            protected List<LocalTime> call() throws Exception {
                return ServiceLocator.getInstance().getBookingService().getAvailableTimeSlots(locationId, date);
            }
        };
        task.setOnSucceeded(e -> showSlots(task.getValue()));
        task.setOnFailed(e -> {
            statusLabel.setText("Non siamo riusciti a caricare gli orari disponibili.");
            LOGGER.log(Level.WARNING, "[booking] failed to load slots for location " + locationId, task.getException());
        });

        Thread thread = new Thread(task, "booking-slots-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to render the available time slots as a group of selectable toggle chips,
     * enabling the "Prenota" button once one is picked.
     *
     * @param slots the available time slots for the selected date, possibly empty if closed
     */
    private void showSlots(List<LocalTime> slots) {
        if (slots.isEmpty()) {
            statusLabel.setText("Il locale è chiuso in questa data.");
            return;
        }
        statusLabel.setText("");
        for (LocalTime slot : slots) {
            ToggleButton button = new ToggleButton(slot.toString());
            button.setUserData(slot);
            button.setToggleGroup(slotGroup);
            button.selectedProperty().addListener((obs, wasSelected, isSelected) -> bookButton.setDisable(slotGroup.getSelectedToggle() == null));
            slotsPane.getChildren().add(button);
        }
    }
}
