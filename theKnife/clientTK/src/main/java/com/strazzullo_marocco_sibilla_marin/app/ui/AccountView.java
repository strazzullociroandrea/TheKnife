package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import strazzullo.User;

/**
 * Account screen showing the logged-in user's profile data and a logout button.
 * Shares the same two-panel layout as {@link LoginView}: a black left branding panel and a
 * white right content panel.
 *
 * @version 2.0
 * @Author Marocco Stefano, 762192, VA
 */
public class AccountView extends HBox {

    /**
     * AccountView constructor.
     *
     * @param shell the app shell, used for navigation and to read the current user
     */
    public AccountView(AppShell shell) {
        User user = shell.getCurrentUser();
        String roleLabel = "manager".equals(user.getRole()) ? "Ristoratore" : "Cliente";

        // Left panel
        VBox leftPanel = new VBox(20);
        leftPanel.setStyle("-fx-background-color: #000000;");
        leftPanel.setPrefWidth(500);
        leftPanel.setPadding(new Insets(40));

        Label backLabel = new Label("←");
        backLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        backLabel.setCursor(Cursor.HAND);
        backLabel.setOnMouseClicked(e -> shell.goBack());

        Label appName = new Label("TheKnife");
        appName.getStyleClass().add(Styles.TITLE_2);
        appName.setStyle("-fx-text-fill: white;");

        HBox topRow = new HBox(10, backLabel, appName);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label title = new Label(user.getName() + " " + user.getSurname());
        title.getStyleClass().add(Styles.TITLE_1);
        title.setStyle("-fx-text-fill: white;");
        title.setWrapText(true);

        Label subtitle = new Label(roleLabel);
        subtitle.getStyleClass().add(Styles.TEXT_CAPTION);
        subtitle.setStyle("-fx-text-fill: white;");

        leftPanel.getChildren().addAll(topRow, spacer, title, subtitle);

        // Right panel — vertically centered like LoginView
        VBox rightPanel = new VBox(12);
        rightPanel.setStyle("-fx-background-color: #ffffff;");
        rightPanel.setPadding(new Insets(0, 100, 0, 100));
        rightPanel.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label("Il tuo profilo");
        heading.getStyleClass().add(Styles.TITLE_2);

        Separator sep = new Separator();
        sep.setMaxWidth(Double.MAX_VALUE);

        rightPanel.getChildren().addAll(
                heading,
                sep,
                buildField("Nome", user.getName()),
                buildField("Cognome", user.getSurname()),
                buildField("Email", user.getEmail()),
                buildField("Indirizzo", user.getDomicile()),
                buildField("Ruolo", roleLabel)
        );

        if (user.getDateOfBirth() != null && !user.getDateOfBirth().isBlank()) {
            rightPanel.getChildren().add(buildField("Data di nascita", user.getDateOfBirth()));
        }

        Button logoutButton = new Button("Esci da TheKnife");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setPrefHeight(40);
        logoutButton.getStyleClass().addAll(Styles.TEXT_NORMAL, Styles.DANGER);
        logoutButton.setCursor(Cursor.HAND);
        logoutButton.setOnAction(e -> shell.logout());

        rightPanel.getChildren().add(logoutButton);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        getChildren().addAll(leftPanel, rightPanel);
    }

    private VBox buildField(String labelText, String value) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add(Styles.TEXT_SMALL);
        lbl.setStyle("-fx-text-fill: grey;");

        Label val = new Label(value != null ? value : "—");
        val.getStyleClass().add(Styles.TEXT_NORMAL);

        return new VBox(2, lbl, val);
    }
}
