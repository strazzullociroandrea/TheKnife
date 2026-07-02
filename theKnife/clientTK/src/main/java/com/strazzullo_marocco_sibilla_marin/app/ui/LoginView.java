package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.remote.LoginResult;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.session.SessionStore;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.MessageBanner;
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
import strazzullo.*;

/**
 * Login page where the user can authenticate or, if they do not have credentials yet, proceed
 * to the registration screen. Uses the same two-panel layout as {@link RegistrationView}: a black
 * left branding panel and a white right form panel.
 * After a successful login the session token is persisted to disk via {@link SessionStore} so the
 * app can restore the session on the next start without prompting the user again.
 *
 * @version 3.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class LoginView extends HBox {

    /**
     * Email input field.
     */
    private final TextField emailField;

    /**
     * Password input field.
     */
    private final PasswordField passwordField;

    /**
     * Feedback banner, hidden until a login attempt completes.
     */
    private final MessageBanner banner = new MessageBanner();

    /**
     * LoginView constructor.
     *
     * @param shell the app shell used for navigation and to store the authenticated session
     */
    public LoginView(AppShell shell) {
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

        Label title = new Label("Prenota i migliori ristoranti della tua città.");
        title.getStyleClass().add(Styles.TITLE_1);
        title.setStyle("-fx-text-fill: white;");
        title.setWrapText(true);
        Label subtitle = new Label("Accedi per gestire le tue prenotazioni, salvare i preferiti e scoprire nuovi locali.");
        subtitle.getStyleClass().add(Styles.TEXT_CAPTION);
        subtitle.setStyle("-fx-text-fill: white;");
        subtitle.setWrapText(true);
        leftPanel.getChildren().addAll(topRow, spacer, title, subtitle);

        VBox rightPanel = new VBox(10);
        rightPanel.setStyle("-fx-background-color: #ffffff;");
        rightPanel.setPadding(new Insets(0, 100, 0, 100));
        rightPanel.setAlignment(Pos.CENTER_LEFT);

        Label greet = new Label("Bentornato");
        greet.getStyleClass().add(Styles.TITLE_1);

        Label info = new Label("Accedi al tuo account TheKnife.");
        info.getStyleClass().add(Styles.TEXT_CAPTION);
        info.setStyle("-fx-text-fill: grey;");

        banner.setMaxWidth(Double.MAX_VALUE);

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
        login.setOnMouseClicked(e -> {
            banner.hide();

            this.handleLoginUser(shell);
        });

        Text labelReg = new Text("Non hai un account? ");
        labelReg.getStyleClass().add(Styles.TEXT);
        Text reg = new Text("Registrati");
        reg.setFill(Color.GREEN);
        reg.setCursor(Cursor.HAND);
        reg.setOnMouseClicked(event -> shell.showRegistrationView());

        TextFlow gotoRegisterPage = new TextFlow(labelReg, reg);
        HBox registerContainer = new HBox(gotoRegisterPage);
        registerContainer.setAlignment(Pos.CENTER);

        rightPanel.getChildren().addAll(greet, info, banner, email, emailField, password, passwordField, login, registerContainer);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        getChildren().addAll(leftPanel, rightPanel);
    }

    /**
     * Validates the form inputs, performs the RMI login call, persists the session token on
     * success, and delegates navigation back to the shell. Shows an inline error message on any
     * failure without navigating away.
     *
     * @param shell the app shell used to store the authenticated session and navigate back
     */
    private void handleLoginUser(AppShell shell) {
        try {
            String email = emailField.getText();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                banner.showError("Attenzione. Tutti i campi sono obbligatori.");
            } else {
                LoginResult result = ServiceLocator.getInstance().getAuthService().login(email, password);
                if (result == null) {
                    banner.showError("Attenzione. Verifica le credenziali inserite.");
                } else {
                    emailField.clear();
                    passwordField.clear();
                    SessionStore.save(result.getSessionToken());
                    shell.setSession(result.getUser(), result.getSessionToken());
                    shell.goBackAfterAuthChange();
                }
            }
        } catch (Exception e) {
            banner.showError("Attenzione. Non è stato possibile proseguire con l'autenticazione.");
        }
    }
}
