package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * The toolbar's account entry point, shared by {@link HomeToolbar} and {@link SearchToolbar} so
 * both read and behave identically: a labeled "Accedi" button for guests/managers (unambiguous
 * about what it does), switching to an icon-only avatar once a user is logged in.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class AccountButton extends Button {

    /**
     * AccountButton constructor.
     *
     * @param loggedIn whether a user is currently logged in
     * @param onClick callback invoked when the button is pressed
     */
    public AccountButton(boolean loggedIn, Runnable onClick) {
        if (loggedIn) {
            setGraphic(new FontIcon(Feather.USER));
            getStyleClass().addAll(Styles.BUTTON_ICON, Styles.BUTTON_CIRCLE, Styles.BUTTON_OUTLINED, "tk-account-button");
            Tooltip tooltip = new Tooltip("Account");
            tooltip.setShowDelay(Duration.millis(150));
            setTooltip(tooltip);
        } else {
            setText("Accedi");
            setGraphic(new FontIcon(Feather.LOG_IN));
            getStyleClass().add(Styles.BUTTON_OUTLINED);
        }
        setOnAction(e -> onClick.run());
    }
}
