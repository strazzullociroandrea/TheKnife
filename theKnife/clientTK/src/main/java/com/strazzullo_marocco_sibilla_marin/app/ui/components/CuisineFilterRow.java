package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.Cuisine;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The search screen's quick cuisine filter row: one circular badge chip per {@link Cuisine},
 * plus the "Filtri" button that opens the advanced {@link FilterPanel}. Owns only its own chip
 * selection state — the actual {@link com.strazzullo_marocco_sibilla_marin.app.ui.AdvancedFilters}
 * it feeds into lives in the host screen, which is told about changes via {@code onCuisineChanged}
 * and can push external changes back in via {@link #syncSelection(Cuisine)}.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class CuisineFilterRow extends HBox {

    private final Map<Cuisine, ToggleButton> chips = new LinkedHashMap<>();
    private final Button filtersButton = new Button("Filtri", new FontIcon(Feather.SLIDERS));

    /**
     * CuisineFilterRow constructor.
     *
     * @param onCuisineChanged callback invoked with the newly selected cuisine, or null when
     *                         a chip is deselected
     * @param onFiltersClick callback invoked when the "Filtri" button is pressed
     */
    public CuisineFilterRow(Consumer<Cuisine> onCuisineChanged, Runnable onFiltersClick) {
        setSpacing(6);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(4, 24, 16, 24));

        for (Cuisine cuisine : Cuisine.values()) {
            ToggleButton chip = buildChip(cuisine, cuisineIcon(cuisine), label(cuisine));
            chip.setOnAction(e -> {
                Cuisine selected = chip.isSelected() ? cuisine : null;
                syncSelection(selected);
                onCuisineChanged.accept(selected);
            });
            chips.put(cuisine, chip);
            getChildren().add(chip);
        }

        StackPane filtersCircle = circleBadge(new FontIcon(Feather.SLIDERS));
        filtersButton.setGraphic(filtersCircle);
        filtersButton.setContentDisplay(ContentDisplay.TOP);
        filtersButton.getStyleClass().add("tk-cuisine-toggle");
        installHoverLift(filtersButton);
        filtersButton.setOnAction(e -> onFiltersClick.run());
        getChildren().add(filtersButton);
    }

    /**
     * Function to select (or clear, if null) the chip for a cuisine without firing
     * {@code onCuisineChanged}, used to keep this row in sync when the cuisine is changed
     * elsewhere (the advanced {@link FilterPanel} also edits it).
     *
     * @param cuisine the cuisine to select, or null to clear the selection
     */
    public void syncSelection(Cuisine cuisine) {
        chips.forEach((c, chip) -> chip.setSelected(c == cuisine));
    }

    /**
     * Function to reflect whether any advanced filter (beyond cuisine) is currently active in
     * the "Filtri" button's label.
     *
     * @param active true if at least one advanced filter is set
     */
    public void setFiltersActive(boolean active) {
        filtersButton.setText(active ? "Filtri (attivi)" : "Filtri");
    }

    /**
     * Function to build one cuisine's toggle chip: an icon badge above a text label, reflecting
     * its selected state via the {@code selected} pseudo-class for CSS styling.
     *
     * @param cuisine the cuisine this chip selects
     * @param icon the chip's icon
     * @param text the chip's label
     * @return the chip
     */
    private ToggleButton buildChip(Cuisine cuisine, Node icon, String text) {
        StackPane circle = circleBadge(icon);
        ToggleButton chip = new ToggleButton(text);
        chip.setGraphic(circle);
        chip.setContentDisplay(ContentDisplay.TOP);
        chip.getStyleClass().add("tk-cuisine-toggle");
        chip.selectedProperty().addListener((obs, wasSelected, isSelected) ->
                chip.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("selected"), isSelected));
        installHoverLift(chip);
        return chip;
    }

    /**
     * Function to wrap an icon in the fixed-size circular badge shared by every chip.
     *
     * @param icon the icon to wrap
     * @return the circular badge
     */
    private StackPane circleBadge(Node icon) {
        StackPane circle = new StackPane(icon);
        circle.getStyleClass().add("tk-cuisine-circle");
        circle.setPrefSize(56, 56);
        circle.setMinSize(56, 56);
        circle.setMaxSize(56, 56);
        return circle;
    }

    /**
     * Function to add a subtle scale-up hover animation to a node, easing out on entry and exit.
     *
     * @param node the node to animate on hover
     */
    private void installHoverLift(Node node) {
        ScaleTransition grow = new ScaleTransition(Duration.millis(140), node);
        grow.setToX(1.08);
        grow.setToY(1.08);
        grow.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        ScaleTransition shrink = new ScaleTransition(Duration.millis(140), node);
        shrink.setToX(1.0);
        shrink.setToY(1.0);
        shrink.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        node.setOnMouseEntered(e -> {
            shrink.stop();
            grow.playFromStart();
        });
        node.setOnMouseExited(e -> {
            grow.stop();
            shrink.playFromStart();
        });
    }

    /**
     * Function to resolve the Italian display label for a quick-filter cuisine chip.
     *
     * @param cuisine the cuisine
     * @return the Italian label
     */
    private String label(Cuisine cuisine) {
        return CuisineLabels.of(cuisine);
    }

    /**
     * Function to load the colored 3D icon for a quick-filter cuisine chip, bundled under
     * {@code /icons/cuisine/<cuisine name>.png}.
     *
     * @param cuisine the cuisine
     * @return an image view of the cuisine's icon, sized for its circular badge
     */
    private ImageView cuisineIcon(Cuisine cuisine) {
        Image image = new Image(getClass().getResourceAsStream("/icons/cuisine/" + cuisine.name() + ".png"));
        ImageView view = new ImageView(image);
        view.setFitWidth(32);
        view.setFitHeight(32);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }
}
