package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * The home screen's top toolbar, mirroring {@link SearchToolbar}'s look: the "TheKnife" wordmark,
 * a single unified search pill (city + free-text query), the search button, round icon-only
 * "Prenotazioni" and "Preferiti" buttons (shown only for logged-in customers, same round style as
 * the search/account buttons so the whole bar reads as one family), and the account button. The
 * account button reads "Accedi" for guests/managers and switches to an icon-only avatar once
 * logged in, so it's unambiguous which state it will take you to.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class HomeToolbar extends HBox {

    private final TextField cityField = new TextField();
    private final TextField queryField = new TextField();

    /**
     * HomeToolbar constructor.
     *
     * @param onSearch callback invoked when the search button is pressed, or Enter in either field
     * @param onAccountClick callback invoked when the account button is pressed
     * @param loggedIn whether a user is currently logged in, switching the account button between
     *                 a labeled "Accedi" button and an icon-only avatar
     * @param showCustomerButtons whether to show the "Prenotazioni" and "Preferiti" buttons
     *                            (logged-in customers only)
     * @param onBookingsClick callback invoked when the "Prenotazioni" button is pressed
     * @param onFavouritesClick callback invoked when the "Preferiti" button is pressed
     */
    public HomeToolbar(Runnable onSearch, Runnable onAccountClick, boolean loggedIn,
                        boolean showCustomerButtons, Runnable onBookingsClick, Runnable onFavouritesClick) {
        setSpacing(14);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(16, 24, 12, 24));

        cityField.setPromptText("Città");
        cityField.setPrefWidth(140);
        cityField.setMinWidth(100);

        queryField.setPromptText("Cerca ristorante, cucina, zona...");
        queryField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                onSearch.run();
            }
        });
        cityField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                onSearch.run();
            }
        });

        Label logo = new Label("TheKnife");
        logo.getStyleClass().add(Styles.TITLE_3);

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

        getChildren().addAll(logo, pill, searchButton);

        if (showCustomerButtons) {
            getChildren().add(buildRoundIconButton(Feather.CALENDAR, "Prenotazioni", onBookingsClick));
            getChildren().add(buildRoundIconButton(Feather.HEART, "Preferiti", onFavouritesClick));
        }

        getChildren().add(new AccountButton(loggedIn, onAccountClick));
    }

    /**
     * Function to build a round, icon-only toolbar button, styled like the search and account
     * buttons so "Prenotazioni" and "Preferiti" read as part of the same button family instead of
     * standing out as plain labeled buttons. The label only survives as a hover tooltip.
     *
     * @param icon the icon to show
     * @param tooltipText the tooltip shown on hover, naming what the button does
     * @param onClick callback invoked when the button is pressed
     * @return the button
     */
    private Button buildRoundIconButton(Feather icon, String tooltipText, Runnable onClick) {
        Button button = new Button("", new FontIcon(icon));
        button.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.BUTTON_CIRCLE, Styles.BUTTON_OUTLINED, "tk-account-button");
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.millis(150));
        button.setTooltip(tooltip);
        button.setOnAction(e -> onClick.run());
        return button;
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
