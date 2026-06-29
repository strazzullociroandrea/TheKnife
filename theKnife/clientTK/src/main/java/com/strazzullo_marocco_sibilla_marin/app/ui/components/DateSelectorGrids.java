package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Builds {@link DateSelector}'s two calendar grids: the day grid for a displayed month, and the
 * month grid used as a fast year-wide jump. Kept stateless and separate so {@link DateSelector}
 * itself is left to own the popover/navigation plumbing around them.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
final class DateSelectorGrids {

    private static final String[] WEEKDAY_HEADERS = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
    private static final double CELL_SIZE = 40;

    private DateSelectorGrids() {
    }

    /**
     * @param month      the month to lay out
     * @param selected   the currently selected date, highlighted if it falls in this month
     * @param isDisabled predicate returning {@code true} for dates that should be greyed out and
     *                   unclickable (e.g. past dates for booking, future dates for date of birth)
     * @param onPick     called with the picked date when an enabled day cell is clicked
     * @return the day grid, with a weekday header row above it
     */
    static VBox buildDayGrid(YearMonth month, LocalDate selected, Predicate<LocalDate> isDisabled,
                             Consumer<LocalDate> onPick) {
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

        LocalDate firstOfMonth = month.atDay(1);
        int leadingBlanks = firstOfMonth.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        int row = 0;
        int column = leadingBlanks;
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            grid.add(buildDayCell(month.atDay(day), selected, isDisabled, onPick), column, row);
            column++;
            if (column == 7) {
                column = 0;
                row++;
            }
        }

        return new VBox(weekdaysRow, grid);
    }

    private static ToggleButton buildDayCell(LocalDate date, LocalDate selected,
                                              Predicate<LocalDate> isDisabled, Consumer<LocalDate> onPick) {
        ToggleButton button = new ToggleButton(String.valueOf(date.getDayOfMonth()));
        button.getStyleClass().add("tk-date-cell");
        button.setMinSize(CELL_SIZE, 36);
        button.setMaxSize(CELL_SIZE, 36);
        if (date.equals(LocalDate.now())) {
            button.getStyleClass().add("tk-date-cell-today");
        }
        if (isDisabled.test(date)) {
            button.setDisable(true);
        }
        button.setSelected(selected != null && date.equals(selected));
        button.setOnAction(e -> onPick.accept(date));
        return button;
    }

    /**
     * @param displayedMonth the year to lay out months for, and the month to highlight as selected
     * @param onPick called with the picked month when a month cell is clicked
     * @return the month grid
     */
    static GridPane buildMonthGrid(YearMonth displayedMonth, Consumer<Month> onPick) {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));
        for (int i = 0; i < 3; i++) {
            grid.getColumnConstraints().add(new ColumnConstraints(2.5 * CELL_SIZE));
        }

        Month[] months = Month.values();
        for (int i = 0; i < months.length; i++) {
            grid.add(buildMonthCell(months[i], displayedMonth, onPick), i % 3, i / 3);
        }
        return grid;
    }

    private static ToggleButton buildMonthCell(Month month, YearMonth displayedMonth, Consumer<Month> onPick) {
        ToggleButton button = new ToggleButton(Capitalization.capitalize(month.getDisplayName(TextStyle.SHORT, Locale.ITALIAN)));
        button.getStyleClass().add("tk-date-cell");
        button.setMinHeight(40);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setSelected(month == displayedMonth.getMonth());
        button.setOnAction(e -> onPick.accept(month));
        return button;
    }
}
