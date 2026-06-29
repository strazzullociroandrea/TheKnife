package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import strazzullo.*;

/**
 * Login page where the user can log in or, if they do not have credentials, proceed
 * with his registration.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class LoginView extends HBox {

    /**
     * Private data used for user login
     */
    private final TextField emailField;
    private final PasswordField passwordField;

    /**
     * Variable to show  message
     */
    private final Label showMessage;

    /**
     * Login view constructor
     *
     * @param shell the app shell to navigate through once the search is submitted
     */
    public LoginView(AppShell shell) {

        //Left panel - Introduction
        VBox leftPanel = new VBox(20);
        leftPanel.setStyle("-fx-background-color: #000000;");
        leftPanel.setPrefWidth(500);
        leftPanel.setPadding(new Insets(40));

        FontIcon backIcon = new FontIcon(Feather.ARROW_LEFT);
        backIcon.setIconColor(Paint.valueOf("white"));
        Button backButton = new Button("", backIcon);
        backButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        backButton.setCursor(Cursor.HAND);
        backButton.setOnAction(e -> shell.goBack());

        Label appName = new Label("TheKnife");
        appName.getStyleClass().add(Styles.TITLE_2);
        appName.setStyle("-fx-text-fill: white;");

        HBox topRow = new HBox(8, backButton, appName);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label title = new Label("Prenota i migliori ristoranti della tua città.");
        title.getStyleClass().add(Styles.TITLE_1);
        title.setStyle("-fx-text-fill: white;");
        title.setWrapText(true);
        Label subtitle = new Label("Accedi per gestire le tue prenotazioni, salvare i preferiti e scoprire nuovi locali.");
        subtitle.getStyleClass().add(Styles.TEXT_CAPTION);
        subtitle.setStyle("-fx-text-fill: white;");
        subtitle.setWrapText(true);
        leftPanel.getChildren().addAll(topRow, spacer, title, subtitle);

        //Right panel - login form
        VBox rightPanel = new VBox(10);
        rightPanel.setStyle("-fx-background-color: #ffffff;");
        rightPanel.setPadding(new Insets(0, 100, 0, 100));
        rightPanel.setAlignment(Pos.CENTER_LEFT);

        VBox formContent = rightPanel;

        Label greet = new Label("Bentornato");
        greet.getStyleClass().add(Styles.TITLE_1);

        Label info = new Label("Accedi al tuo account TheKnife.");
        info.getStyleClass().add(Styles.TEXT_CAPTION);
        info.setStyle("-fx-text-fill: grey;");

        //Message row invisible - input form login
        showMessage = new Label();
        showMessage.setMaxWidth(Double.MAX_VALUE);
        showMessage.setVisible(false);

        Label email = new Label("Email *");
        email.setStyle("-fx-text-fill: grey;");
        email.getStyleClass().add(Styles.TEXT_SMALL);
        emailField = new TextField();
        emailField.setPromptText("marco.rossi@email.it");
        Label password = new Label("Password *");
        password.setStyle("-fx-text-fill: grey;");
        password.getStyleClass().add(Styles.TEXT_SMALL);

        passwordField = new PasswordField();
        passwordField.setPromptText("••••••••••••");
        Button login = new Button("Accedi");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setPrefHeight(40);
        login.getStyleClass().add(Styles.TEXT_NORMAL);
        login.setStyle("-fx-background-color: #000000; -fx-text-fill: white; ");
        login.setCursor(Cursor.HAND);

        login.setOnMouseClicked(e -> this.handleLoginUser(shell));

        Text labelReg = new Text("Non hai un account? ");
        labelReg.getStyleClass().add(Styles.TEXT);
        Text reg = new Text("Registrati");
        reg.setFill(Color.GREEN);
        reg.setCursor(Cursor.HAND);
        reg.setOnMouseClicked(event -> shell.showRegistrationView());

        TextFlow gotoRegisterPage = new TextFlow(labelReg, reg);
        HBox registerContainer = new HBox(gotoRegisterPage);
        registerContainer.setAlignment(Pos.CENTER);

        formContent.getChildren().addAll(greet, info, showMessage, email, emailField, password, passwordField, login, registerContainer);

        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        getChildren().addAll(leftPanel, rightPanel);


    }

    /**
     * Private function that allows user login
     *
     * @param shell the app shell to set the user id
     */
    private void handleLoginUser(AppShell shell) {
        try {
            String email = emailField.getText(),
                    password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                showMessage.setText("Attenzione. Tutti i campi sono obbligatori.");
                showMessage.setStyle("-fx-text-fill: red;");
                showMessage.setVisible(true);
            } else {
                showMessage.setText("Login avvenuto con successo");
                showMessage.setStyle("-fx-text-fill: green;");
                showMessage.setVisible(true);
                User u = ServiceLocator.getInstance().getAuthService().login(email, password);
                if (u == null) {
                    showMessage.setText("Attenzione. Non è stato trovato nessun account con le credenziali inserite.");
                    showMessage.setStyle("-fx-text-fill: red;");
                    showMessage.setVisible(true);
                } else {
                    shell.setCurrentUserId(u.getId());
                    shell.goBack();
                }
            }
        } catch (Exception e) {
            showMessage.setText("Attenzione." + e.getMessage());
            showMessage.setStyle("-fx-text-fill: red;");
            showMessage.setVisible(true);
        } finally {
            emailField.clear();
            passwordField.clear();
        }
    }
}