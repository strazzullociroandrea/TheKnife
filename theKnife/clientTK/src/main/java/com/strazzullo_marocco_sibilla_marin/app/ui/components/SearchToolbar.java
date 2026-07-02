package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * The search screen's top toolbar: the "TheKnife" logo (which navigates home), a single big
 * free-text search field (clearable via its trailing "x" button once it has text), the search
 * button, and the {@link AccountButton} — shared with {@link HomeToolbar} so both toolbars'
 * account entry point reads and behaves identically. This field matches restaurant name, location
 * name, city, and address at once; searching near a specific resolved position is a separate
 * concern, handled by the results screen's own "Distanza da un indirizzo" row instead.
 *
 * @version 5.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class SearchToolbar extends HBox {

    private final TextField queryField = new TextField();

    /**
     * SearchToolbar constructor.
     *
     * @param initialQuery the free-text query to pre-fill the field with, may be blank
     * @param onLogoClick callback invoked when the logo is clicked
     * @param onSearch callback invoked when the search button is pressed, or Enter in the field
     * @param onAccountClick callback invoked when the account button is pressed
     * @param loggedIn whether a user is currently logged in, switching the account button between
     *                 a labeled "Accedi" button and an icon-only avatar
     */
    public SearchToolbar(String initialQuery, Runnable onLogoClick, Runnable onSearch,
                          Runnable onAccountClick, boolean loggedIn) {
        setSpacing(14);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(16, 24, 12, 24));

        queryField.setText(initialQuery == null ? "" : initialQuery);
        queryField.setPromptText("Cerca ristorante, cucina, città, zona...");
        queryField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                onSearch.run();
            }
        });

        Label logo = new Label("TheKnife");
        logo.getStyleClass().add(Styles.TITLE_3);
        logo.setOnMouseClicked(e -> onLogoClick.run());
        logo.setCursor(javafx.scene.Cursor.HAND);

        Button clearButton = new Button("", new FontIcon(Feather.X));
        clearButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.BUTTON_CIRCLE);
        clearButton.visibleProperty().bind(Bindings.isNotEmpty(queryField.textProperty()));
        clearButton.managedProperty().bind(clearButton.visibleProperty());
        clearButton.setOnAction(e -> {
            queryField.clear();
            onSearch.run();
        });

        HBox pill = new HBox(10, new FontIcon(Feather.SEARCH), queryField, clearButton);
        pill.getStyleClass().add("tk-search-pill");
        pill.setAlignment(Pos.CENTER_LEFT);
        pill.setPadding(new Insets(10, 18, 10, 18));
        HBox.setHgrow(pill, Priority.ALWAYS);
        HBox.setHgrow(queryField, Priority.ALWAYS);

        Button searchButton = new Button("", new FontIcon(Feather.SEARCH));
        searchButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.BUTTON_CIRCLE, "tk-toolbar-search-button");
        searchButton.setOnAction(e -> onSearch.run());

        getChildren().addAll(logo, pill, searchButton, new AccountButton(loggedIn, onAccountClick));
    }

    /**
     * @return the search field's current text
     */
    public String getQuery() {
        return queryField.getText();
    }
}
