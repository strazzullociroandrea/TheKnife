package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * The search screen's top toolbar: the "TheKnife" logo (which navigates home), a single unified
 * search pill (city + free-text query), the search button, and the {@link AccountButton} —
 * shared with {@link HomeToolbar} so both toolbars' account entry point reads and behaves
 * identically.
 *
 * @version 3.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class SearchToolbar extends HBox {

    private final TextField cityField = new TextField();
    private final TextField queryField = new TextField();

    /**
     * SearchToolbar constructor.
     *
     * @param initialCity the city to pre-fill the city field with, may be blank
     * @param initialQuery the free-text query to pre-fill the query field with, may be blank
     * @param onLogoClick callback invoked when the logo is clicked
     * @param onSearch callback invoked when the search button is pressed, or Enter in either field
     * @param onAccountClick callback invoked when the account button is pressed
     * @param loggedIn whether a user is currently logged in, switching the account button between
     *                 a labeled "Accedi" button and an icon-only avatar
     */
    public SearchToolbar(String initialCity, String initialQuery, Runnable onLogoClick, Runnable onSearch,
                          Runnable onAccountClick, boolean loggedIn) {
        setSpacing(14);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(16, 24, 12, 24));

        cityField.setText(initialCity == null ? "" : initialCity);
        cityField.setPromptText("Città");
        cityField.setPrefWidth(140);
        cityField.setMinWidth(100);

        queryField.setText(initialQuery == null ? "" : initialQuery);
        queryField.setPromptText("Cerca per nome del ristorante...");
        queryField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                onSearch.run();
            }
        });

        Label logo = new Label("TheKnife");
        logo.getStyleClass().add(Styles.TITLE_3);
        logo.setOnMouseClicked(e -> onLogoClick.run());
        logo.setCursor(javafx.scene.Cursor.HAND);

        Region divider = new Region();
        divider.getStyleClass().add("tk-search-divider");
        divider.setMinSize(1, 22);
        divider.setMaxSize(1, 22);

        HBox pill = new HBox(10,
                new FontIcon(Feather.MAP_PIN), cityField,
                divider,
                new FontIcon(Feather.SEARCH), queryField);
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
     * @return the city field's current text
     */
    public String getCity() {
        return cityField.getText();
    }

    /**
     * @return the query field's current text
     */
    public String getQuery() {
        return queryField.getText();
    }
}
