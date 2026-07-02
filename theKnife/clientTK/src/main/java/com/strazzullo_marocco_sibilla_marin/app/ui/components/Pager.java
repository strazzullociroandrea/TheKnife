package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A small reusable Prev/Next pager: a "Precedente" button, a "Pagina N" label, and a "Successiva"
 * button, centered in a row. Used by any screen paginating a list via {@code SearchFilter.page},
 * which enables/disables the buttons through {@link #setState(int, boolean, boolean)} rather than
 * this control tracking page state itself.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class Pager extends HBox {

    private final Button prevButton = new Button("Precedente", new FontIcon(Feather.CHEVRON_LEFT));
    private final Button nextButton = new Button("Successiva", new FontIcon(Feather.CHEVRON_RIGHT));
    private final Label pageLabel = new Label();

    /**
     * Pager constructor.
     *
     * @param onPrev callback invoked when the "Precedente" button is pressed
     * @param onNext callback invoked when the "Successiva" button is pressed
     */
    public Pager(Runnable onPrev, Runnable onNext) {
        setAlignment(Pos.CENTER);
        setSpacing(16);
        setPadding(new Insets(16, 0, 16, 0));

        prevButton.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        prevButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        prevButton.setOnAction(e -> onPrev.run());

        nextButton.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        nextButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        nextButton.setOnAction(e -> onNext.run());

        pageLabel.getStyleClass().add(Styles.TEXT_MUTED);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        getChildren().addAll(leftSpacer, prevButton, pageLabel, nextButton, rightSpacer);
        setState(0, false, false);
    }

    /**
     * Function to update the displayed page number and enable/disable the buttons.
     *
     * @param pageNumber zero-based page index currently shown
     * @param hasPrev whether a previous page exists
     * @param hasNext whether a next page exists
     */
    public void setState(int pageNumber, boolean hasPrev, boolean hasNext) {
        pageLabel.setText("Pagina " + (pageNumber + 1));
        prevButton.setDisable(!hasPrev);
        nextButton.setDisable(!hasNext);
    }
}
