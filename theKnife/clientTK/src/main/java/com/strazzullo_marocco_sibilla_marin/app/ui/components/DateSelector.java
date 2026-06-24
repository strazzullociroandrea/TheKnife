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
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

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

    private static final String[] WEEKDAY_HEADERS = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
    private static final DateTimeFormatter FIELD_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double CELL_SIZE = 40;
    private static final double POPOVER_CORNER_RADIUS = 16;

    private final ObjectProperty<LocalDate> value;
    private final Button field = new Button();
    private final Popover popover = new Popover();
    private final Label headerLabel = new Label();
    private final Button prevButton = navButton(Feather.CHEVRON_LEFT);
    private final Button nextButton = navButton(Feather.CHEVRON_RIGHT);
    private final VBox calendarBody = new VBox();

    private YearMonth displayedMonth;
    private boolean showingMonthGrid;

    /**
     * DateSelector constructor.
     *
     * @param initialValue the initially selected date
     */
    public DateSelector(LocalDate initialValue) {
        value = new SimpleObjectProperty<>(initialValue);
        displayedMonth = YearMonth.from(initialValue);

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
        // No arrow nub: it's a small triangular cutout in the rounded-rect Path that doesn't line
        // up with this content's own clip, leaving visible triangular slivers at the corners.
        popover.setArrowSize(0);
        popover.setArrowIndent(0);

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

    private void openPopover() {
        displayedMonth = YearMonth.from(getValue());
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

        // Popover's own rounded-rect Path sits *behind* this content, so this opaque background
        // fully covers it; without its own clip here, this rectangle's square corners would show
        // up on top of the Path's rounded ones. Round this content directly instead of relying
        // on Popover's shape showing through.
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(POPOVER_CORNER_RADIUS * 2);
        clip.setArcHeight(POPOVER_CORNER_RADIUS * 2);
        clip.widthProperty().bind(calendar.widthProperty());
        clip.heightProperty().bind(calendar.heightProperty());
        calendar.setClip(clip);

        return calendar;
    }

    private void refresh() {
        headerLabel.setText(showingMonthGrid
                ? String.valueOf(displayedMonth.getYear())
                : Capitalization.capitalize(displayedMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN))
                        + " " + displayedMonth.getYear());
        calendarBody.getChildren().setAll(showingMonthGrid ? buildMonthGrid() : buildDayGrid());
    }

    private VBox buildDayGrid() {
        HBox weekdaysRow = new HBox(4);
        weekdaysRow.setPadding(new Insets(8, 8, 0, 8));
        for (String weekday : WEEKDAY_HEADERS) {
            Label label = new Label(weekday);
            label.getStyleClass().add(Styles.TEXT_MUTED);
            label.setMinWidth(CELL_SIZE);
            label.setAlignment(Pos.CENTER);
            weekdaysRow.getChildren().add(label);
        }

        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setPadding(new Insets(4, 8, 12, 8));
        for (int i = 0; i < 7; i++) {
            grid.getColumnConstraints().add(new ColumnConstraints(CELL_SIZE));
        }

        LocalDate firstOfMonth = displayedMonth.atDay(1);
        int leadingBlanks = firstOfMonth.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        int row = 0;
        int column = leadingBlanks;
        for (int day = 1; day <= displayedMonth.lengthOfMonth(); day++) {
            grid.add(buildDayCell(displayedMonth.atDay(day)), column, row);
            column++;
            if (column == 7) {
                column = 0;
                row++;
            }
        }

        return new VBox(weekdaysRow, grid);
    }

    private ToggleButton buildDayCell(LocalDate date) {
        ToggleButton button = new ToggleButton(String.valueOf(date.getDayOfMonth()));
        button.getStyleClass().add("tk-date-cell");
        button.setMinSize(CELL_SIZE, 36);
        button.setMaxSize(CELL_SIZE, 36);
        if (date.equals(LocalDate.now())) {
            button.getStyleClass().add("tk-date-cell-today");
        }
        if (date.isBefore(LocalDate.now())) {
            button.setDisable(true);
        }
        button.setSelected(date.equals(getValue()));
        button.setOnAction(e -> {
            value.set(date);
            updateFieldText();
            popover.hide();
        });
        return button;
    }

    private GridPane buildMonthGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));
        for (int i = 0; i < 3; i++) {
            grid.getColumnConstraints().add(new ColumnConstraints(2.5 * CELL_SIZE));
        }

        Month[] months = Month.values();
        for (int i = 0; i < months.length; i++) {
            grid.add(buildMonthCell(months[i]), i % 3, i / 3);
        }
        return grid;
    }

    private ToggleButton buildMonthCell(Month month) {
        ToggleButton button = new ToggleButton(Capitalization.capitalize(month.getDisplayName(TextStyle.SHORT, Locale.ITALIAN)));
        button.getStyleClass().add("tk-date-cell");
        button.setMinHeight(40);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setSelected(month == displayedMonth.getMonth());
        button.setOnAction(e -> {
            displayedMonth = YearMonth.of(displayedMonth.getYear(), month);
            showingMonthGrid = false;
            refresh();
        });
        return button;
    }

    private static Button navButton(Feather icon) {
        Button button = new Button("", new FontIcon(icon));
        button.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.BUTTON_CIRCLE);
        return button;
    }

    private void updateFieldText() {
        field.setText(getValue().format(FIELD_FORMAT));
    }
}
