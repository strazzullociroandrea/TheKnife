package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A colored, icon-led banner for success/error feedback (e.g. "Registrazione avvenuta con
 * successo", "Credenziali non valide"), used in place of plain colored text so these messages
 * read as a deliberate part of the screen rather than an easy-to-miss label. Hidden (and
 * unmanaged, so it doesn't reserve layout space) until {@link #showSuccess(String)} or {@link
 * #showError(String)} is called.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class MessageBanner extends HBox {

    private static final String SUCCESS_STYLE = "tk-banner-success";
    private static final String DANGER_STYLE = "tk-banner-danger";

    private final FontIcon icon = new FontIcon();
    private final Label messageLabel = new Label();

    /**
     * MessageBanner constructor. Starts hidden.
     */
    public MessageBanner() {
        getStyleClass().add("tk-banner");
        setSpacing(10);
        setPadding(new Insets(12, 16, 12, 16));
        setAlignment(Pos.CENTER_LEFT);

        messageLabel.getStyleClass().add(Styles.TEXT_BOLD);
        messageLabel.setWrapText(true);
        HBox.setHgrow(messageLabel, Priority.ALWAYS);

        getChildren().addAll(icon, messageLabel);
        hide();
    }

    /**
     * Function to show a success banner.
     *
     * @param message the message to display
     */
    public void showSuccess(String message) {
        show(message, Feather.CHECK_CIRCLE, SUCCESS_STYLE);
    }

    /**
     * Function to show an error banner.
     *
     * @param message the message to display
     */
    public void showError(String message) {
        show(message, Feather.ALERT_CIRCLE, DANGER_STYLE);
    }

    /**
     * Function to hide the banner, so it no longer reserves layout space.
     */
    public void hide() {
        setVisible(false);
        setManaged(false);
    }

    /**
     * Function to display the banner with a given message, icon, and semantic style class.
     *
     * @param message the message to display
     * @param iconCode the icon to show
     * @param styleClass the semantic style class ({@link #SUCCESS_STYLE} or {@link #DANGER_STYLE})
     */
    private void show(String message, Feather iconCode, String styleClass) {
        getStyleClass().removeAll(SUCCESS_STYLE, DANGER_STYLE);
        getStyleClass().add(styleClass);
        icon.setIconCode(iconCode);
        messageLabel.setText(message);
        setVisible(true);
        setManaged(true);
    }
}
