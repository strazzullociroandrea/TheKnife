package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.ui.AdvancedFilters;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.Cuisine;
import sibilla.Day;

import java.util.function.Consumer;

/**
 * In-app panel editing the {@link AdvancedFilters} criteria that don't live in the search bar:
 * price, capacity, dietary menus, delivery/takeaway, day and time of opening, and minimum rating.
 * Cuisine is deliberately not repeated here, since it already has its own quick-filter chips on
 * the search screen. Sections are ordered by how often they matter (rating first, then price,
 * then service/dietary toggles, then opening hours) and each {@link ToggleSwitch} gets its own
 * full-width row rather than being paired with another one in a stretched HBox, which used to
 * leave labels and switches visually disconnected from each other.
 * Shown as a centered modal card (the same {@code tk-modal-card} look used by {@link
 * LocationPromptPanel}) inside an {@code atlantafx.base.controls.ModalPane}, rather than a
 * separate OS dialog window or a full-height side panel, so it reads as one consistent "popup"
 * language across the app instead of two different overlay styles.
 *
 * @version 4.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class FilterPanel extends VBox {

    private final StarRatingPicker minRatingPicker = new StarRatingPicker();
    private final PriceRangePicker priceRangePicker = new PriceRangePicker();
    private final IntegerStepper partySizeStepper = new IntegerStepper(1, 20, 1);
    private final ToggleSwitch deliverySwitch = new ToggleSwitch("Consegna a domicilio");
    private final ToggleSwitch takeawaySwitch = new ToggleSwitch("Da asporto");
    private final ToggleSwitch vegetarianSwitch = new ToggleSwitch("Menu vegetariano");
    private final ToggleSwitch veganSwitch = new ToggleSwitch("Menu vegano");
    private final ToggleSwitch glutenFreeSwitch = new ToggleSwitch("Menu senza glutine");
    private final ComboBox<Day> openDayBox = new ComboBox<>();
    private final ComboBox<String> openTimeBox = new ComboBox<>();

    /**
     * The cuisine criterion isn't edited here (it has its own quick-filter chips), but must be
     * carried through unchanged so applying this panel doesn't silently clear it.
     */
    private Cuisine currentCuisineType;

    /**
     * FilterPanel constructor, pre-filled with the currently active advanced filters.
     *
     * @param current the advanced filters currently applied
     * @param onApply callback invoked with the newly configured filters when "Applica" is pressed
     * @param onClose callback invoked when the panel should be dismissed without applying anything
     */
    public FilterPanel(AdvancedFilters current, Consumer<AdvancedFilters> onApply, Runnable onClose) {
        setPrefWidth(480);
        setMinWidth(480);
        setMaxWidth(480);
        setPrefHeight(640);
        setMaxHeight(640);
        getStyleClass().addAll(Styles.BG_DEFAULT, "tk-modal-card");

        openDayBox.getItems().add(null);
        DayLabels.orderedDays().forEach(openDayBox.getItems()::add);
        openDayBox.setConverter(new DayConverter());

        openTimeBox.getItems().add(null);
        openTimeBox.getItems().addAll(OpeningTimeSlots.all());
        openTimeBox.setDisable(true);
        openDayBox.valueProperty().addListener((obs, oldDay, newDay) -> openTimeBox.setDisable(newDay == null));

        Label title = new Label("Filtri di ricerca");
        title.getStyleClass().add(Styles.TITLE_3);
        Button closeButton = new Button("", new FontIcon(Feather.X));
        closeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.BUTTON_CIRCLE);
        closeButton.setOnAction(e -> onClose.run());
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(title, headerSpacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 20, 16, 24));

        VBox body = new VBox(18,
                FilterFieldLayout.sectionCard("Valutazione minima", minRatingPicker),
                FilterFieldLayout.sectionCard("Quanto vuoi spendere", priceRangePicker),
                FilterFieldLayout.sectionCard("In quanti siete", partySizeStepper),
                FilterFieldLayout.sectionCard("Servizi e preferenze", new VBox(12,
                        deliverySwitch, takeawaySwitch, vegetarianSwitch, veganSwitch, glutenFreeSwitch)),
                FilterFieldLayout.sectionCard("Apertura", FilterFieldLayout.twoColumns(
                        FilterFieldLayout.field("Giorno", openDayBox),
                        FilterFieldLayout.field("Ora", openTimeBox))));
        body.setPadding(new Insets(4, 22, 24, 22));

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add(Styles.BG_DEFAULT);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button resetButton = new Button("Reimposta");
        resetButton.getStyleClass().add(Styles.FLAT);
        resetButton.setOnAction(e -> applyCurrent(AdvancedFilters.empty()));
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        Button applyButton = new Button("Applica");
        applyButton.getStyleClass().add(Styles.ACCENT);
        applyButton.setOnAction(e -> onApply.accept(collect()));
        HBox footer = new HBox(10, resetButton, footerSpacer, applyButton);
        footer.setPadding(new Insets(16, 24, 20, 24));
        footer.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(header, new Separator(), scroll, new Separator(), footer);

        applyCurrent(current);
    }

    /**
     * Function to pre-fill every control from a set of advanced filters.
     *
     * @param filters the filters to apply to the controls
     */
    private void applyCurrent(AdvancedFilters filters) {
        currentCuisineType = filters.cuisineType();
        priceRangePicker.setMaxPrice(filters.maxPriceRange());
        deliverySwitch.setSelected(Boolean.TRUE.equals(filters.delivery()));
        takeawaySwitch.setSelected(Boolean.TRUE.equals(filters.takeaway()));
        partySizeStepper.setValue(filters.minCapacity() == null ? 1 : filters.minCapacity());
        vegetarianSwitch.setSelected(Boolean.TRUE.equals(filters.vegetarianMenu()));
        veganSwitch.setSelected(Boolean.TRUE.equals(filters.veganMenu()));
        glutenFreeSwitch.setSelected(Boolean.TRUE.equals(filters.glutenFreeMenu()));
        openDayBox.setValue(filters.openDay());
        openTimeBox.setValue(filters.openTime());
        minRatingPicker.setValue(filters.minRating() == null ? 0 : filters.minRating().intValue());
    }

    /**
     * Function to collect the current control values into a new {@link AdvancedFilters}.
     * Fields left at their "no filter" default value are mapped back to null. The cuisine
     * criterion is left untouched, since this panel no longer edits it.
     *
     * @return the advanced filters as configured by the user
     */
    private AdvancedFilters collect() {
        Double maxPrice = priceRangePicker.getMaxPrice();
        Integer minCapacity = partySizeStepper.getValue() > 1 ? partySizeStepper.getValue() : null;
        Double minRating = minRatingPicker.getValue() > 0 ? (double) minRatingPicker.getValue() : null;

        return new AdvancedFilters(
                currentCuisineType,
                maxPrice,
                deliverySwitch.isSelected() ? Boolean.TRUE : null,
                takeawaySwitch.isSelected() ? Boolean.TRUE : null,
                minCapacity,
                vegetarianSwitch.isSelected() ? Boolean.TRUE : null,
                veganSwitch.isSelected() ? Boolean.TRUE : null,
                glutenFreeSwitch.isSelected() ? Boolean.TRUE : null,
                openDayBox.getValue(),
                openDayBox.getValue() != null ? openTimeBox.getValue() : null,
                minRating
        );
    }

}
