package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Centered modal card prompting the user to sign in or register to access a feature. Shown
 * inside an {@code atlantafx.base.controls.ModalPane} when a guest attempts an action that
 * requires authentication, such as adding a favourite or booking a table. Two large action
 * cards give quick access to both paths side by side.
 *
 * @version 2.0
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public class AuthPromptCard extends VBox {

    /**
     * AuthPromptCard constructor.
     *
     * @param message    feature-specific body line, e.g. "Per aggiungere ai preferiti, accedi o registrati."
     * @param onClose    callback invoked when the card should be dismissed
     * @param onLogin    callback invoked when "Accedi" is pressed
     * @param onRegister callback invoked when "Registrati" is pressed
     */
    public AuthPromptCard(String message, Runnable onClose, Runnable onLogin, Runnable onRegister) {
        getStyleClass().addAll(Styles.BG_DEFAULT, "tk-modal-card");
        setPrefWidth(420);
        setMinWidth(360);
        setMaxWidth(460);
        setMaxHeight(Region.USE_PREF_SIZE);

        Button closeButton = new Button("", new FontIcon(Feather.X));
        closeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.BUTTON_CIRCLE);
        closeButton.setOnAction(e -> onClose.run());

        Label title = new Label("Accesso richiesto");
        title.getStyleClass().add(Styles.TITLE_4);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox header = new HBox(title, headerSpacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 16, 0, 20));

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add(Styles.TEXT_MUTED);
        messageLabel.setPadding(new Insets(10, 20, 4, 20));

        HBox actionRow = new HBox(12, buildActionCard("Accedi", Feather.LOG_IN, () -> {
            onClose.run();
            onLogin.run();
        }), buildActionCard("Registrati", Feather.USER_PLUS, () -> {
            onClose.run();
            onRegister.run();
        }));
        actionRow.setPadding(new Insets(12, 20, 20, 20));

        getChildren().addAll(header, messageLabel, actionRow);
    }

    private VBox buildActionCard(String label, Feather icon, Runnable action) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(28);

        Label text = new Label(label);
        text.getStyleClass().addAll(Styles.TEXT_BOLD);

        VBox card = new VBox(10, fontIcon, text);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setCursor(Cursor.HAND);
        card.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-muted; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;");
        HBox.setHgrow(card, Priority.ALWAYS);

        card.setOnMouseClicked(e -> action.run());
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: -color-accent-subtle; -fx-border-color: -color-accent-emphasis; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 2; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-muted; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"));

        return card;
    }
}
