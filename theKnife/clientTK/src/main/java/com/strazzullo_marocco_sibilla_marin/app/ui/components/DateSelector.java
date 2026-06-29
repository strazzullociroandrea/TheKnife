package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.controls.Popover;
import atlantafx.base.theme.Styles;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Brand-styled replacement for {@code javafx.scene.control.DatePicker}, whose AtlantaFX popup
 * skin draws an unlabeled, misaligned week-number column that cannot be removed via CSS (it's
 * baked into the skin, not a stylable artifact). Shows the picked date as a rounded pill button;
 * clicking it opens a small custom calendar, built entirely from this app's own components, in
 * an {@code atlantafx.base.controls.Popover}. The header's month/year label doubles as a
 * shortcut into a month grid (with year, rather than month, step arrows) so jumping several
 * months ahead doesn't mean clicking the day arrow over and over.
 *
 * @version 2.0
 * @Author Marocco Stefano, 762192, VA - author of this revision
 */
public class DateSelector extends HBox {

    private static final DateTimeFormatter FIELD_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double CELL_SIZE = 40;
    private static final double POPOVER_CORNER_RADIUS = 16;

    private final ObjectProperty<LocalDate> value;
    private final Predicate<LocalDate> isDisabled;
    private final Button field = new Button();
    private final Popover popover = new Popover();
    private final Label headerLabel = new Label();
    private final Button prevButton = navButton(Feather.CHEVRON_LEFT);
    private final Button nextButton = navButton(Feather.CHEVRON_RIGHT);
    private final VBox calendarBody = new VBox();

    private YearMonth displayedMonth;
    private boolean showingMonthGrid;

    /**
     * DateSelector constructor. Disables past dates by default (suitable for booking future slots).
     * Pass a non-null {@code initialValue} to pre-select a date, or {@code null} to start with
     * no selection (shows "Seleziona data" on the pill button).
     *
     * @param initialValue the initially selected date, or {@code null} for no selection
     */
    public DateSelector(LocalDate initialValue) {
        this(initialValue, date -> date.isBefore(LocalDate.now()));
    }

    /**
     * DateSelector constructor with a custom disabled-date predicate.
     * Pass a non-null {@code initialValue} to pre-select a date, or {@code null} to start with
     * no selection (shows "Seleziona data" on the pill button).
     *
     * @param initialValue the initially selected date, or {@code null} for no selection
     * @param isDisabled   predicate returning {@code true} for dates that should be greyed out and
     *                     unclickable; e.g. {@code date -> date.isAfter(LocalDate.now())} for
     *                     past-only pickers such as date of birth
     */
    public DateSelector(LocalDate initialValue, Predicate<LocalDate> isDisabled) {
        this.isDisabled = isDisabled;
        value = new SimpleObjectProperty<>(initialValue);
        displayedMonth = initialValue != null ? YearMonth.from(initialValue) : YearMonth.now();

        field.getStyleClass().add("tk-date-field");
        field.setMaxWidth(Double.MAX_VALUE);
        field.setAlignment(Pos.CENTER_LEFT);
        field.setGraphic(new FontIcon(Feather.CALENDAR));
        field.setContentDisplay(ContentDisplay.RIGHT);
        field.setOnAction(e -> openPopover());
        HBox.setHgrow(field, Priority.ALWAYS);
        getChildren().add(field);
        updateFieldText();

        popover.setHeaderAlwaysVisible(false);
        popover.setCloseButtonEnabled(false);
        popover.setArrowLocation(Popover.ArrowLocation.TOP_LEFT);
        popover.setDetachable(false);
        popover.setCornerRadius(POPOVER_CORNER_RADIUS);
        disableArrowNub();

        headerLabel.getStyleClass().add(Styles.TEXT_BOLD);
        headerLabel.setCursor(javafx.scene.Cursor.HAND);
        headerLabel.setOnMouseClicked(e -> {
            showingMonthGrid = !showingMonthGrid;
            refresh();
        });
        prevButton.setOnAction(e -> {
            step(-1);
            refresh();
        });
        nextButton.setOnAction(e -> {
            step(1);
            refresh();
        });
    }

    /**
     * @return the property holding the currently selected date, fired whenever a day is picked
     */
    public ObjectProperty<LocalDate> valueProperty() {
        return value;
    }

    /**
     * @return the currently selected date
     */
    public LocalDate getValue() {
        return value.get();
    }

    /**
     * Function to disable the popover's arrow nub: it's a small triangular cutout in the
     * rounded-rect Path that doesn't line up with this content's own clip, leaving visible
     * triangular slivers at the corners.
     */
    private void disableArrowNub() {
        popover.setArrowSize(0);
        popover.setArrowIndent(0);
    }

    private void openPopover() {
        LocalDate current = getValue();
        displayedMonth = current != null ? YearMonth.from(current) : YearMonth.now();
        showingMonthGrid = false;
        popover.setContentNode(buildCalendar());
        popover.show(field);
    }

    /**
     * Function to step the displayed period forward/back: a month at a time on the day grid, a
     * year at a time on the month grid (jumping years is what makes picking a far-off month fast).
     *
     * @param direction -1 to step back, 1 to step forward
     */
    private void step(int direction) {
        displayedMonth = showingMonthGrid ? displayedMonth.plusYears(direction) : displayedMonth.plusMonths(direction);
    }

    private VBox buildCalendar() {
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(prevButton, headerLabel, headerSpacer, nextButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("tk-date-calendar-header");
        header.setPadding(new Insets(10, 12, 10, 14));

        refresh();

        VBox calendar = new VBox(header, calendarBody);
        calendar.setPrefWidth(7 * CELL_SIZE + 16 + 6 * 4);
        calendar.getStyleClass().add("tk-date-calendar");
        clipToRoundedCorners(calendar);

        return calendar;
    }

    /**
     * Function to clip a node to rounded corners matching the popover's own corner radius.
     * Popover's rounded-rect Path sits *behind* its content, so this content's opaque background
     * fully covers it; without its own clip, this node's square corners would show up on top of
     * the Path's rounded ones. Rounding the content directly is simpler than relying on Popover's
     * shape showing through.
     *
     * @param node the node to clip
     */
    private void clipToRoundedCorners(javafx.scene.layout.Region node) {
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(POPOVER_CORNER_RADIUS * 2);
        clip.setArcHeight(POPOVER_CORNER_RADIUS * 2);
        clip.widthProperty().bind(node.widthProperty());
        clip.heightProperty().bind(node.heightProperty());
        node.setClip(clip);
    }

    private void refresh() {
        headerLabel.setText(showingMonthGrid
                ? String.valueOf(displayedMonth.getYear())
                : Capitalization.capitalize(displayedMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN))
                        + " " + displayedMonth.getYear());
        calendarBody.getChildren().setAll(showingMonthGrid
                ? DateSelectorGrids.buildMonthGrid(displayedMonth, this::onMonthPicked)
                : DateSelectorGrids.buildDayGrid(displayedMonth, getValue(), isDisabled, this::onDayPicked));
    }

    private void onDayPicked(LocalDate date) {
        value.set(date);
        updateFieldText();
        popover.hide();
    }

    private void onMonthPicked(Month month) {
        displayedMonth = YearMonth.of(displayedMonth.getYear(), month);
        showingMonthGrid = false;
        refresh();
    }

    private static Button navButton(Feather icon) {
        Button button = new Button("", new FontIcon(icon));
        button.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.BUTTON_CIRCLE);
        return button;
    }

    private void updateFieldText() {
        LocalDate v = getValue();
        field.setText(v != null ? v.format(FIELD_FORMAT) : "Seleziona data");
    }
}
