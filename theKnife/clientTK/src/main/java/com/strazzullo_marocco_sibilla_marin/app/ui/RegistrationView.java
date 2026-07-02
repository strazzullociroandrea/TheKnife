package com.strazzullo_marocco_sibilla_marin.app.ui;

import com.strazzullo_marocco_sibilla_marin.app.geo.AddressAutocomplete;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.DateSelector;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.MessageBanner;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.RoleCard;
import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import strazzullo.*;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Registration page where the user can register or, if they already have an account, log in.
 * The address field uses {@link AddressAutocomplete} for live Google/Photon suggestions, and the
 * date-of-birth field uses the brand-styled {@link DateSelector} instead of the native
 * {@code javafx.scene.control.DatePicker}.
 *
 * @version 3.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class RegistrationView extends HBox {

    private final RoleCard client, manager;
    private final TextField nameField, surnameField, emailField, placeField;
    private final PasswordField passwordField, confirmField;
    private final DateSelector dateBirthField;
    private final MessageBanner banner = new MessageBanner();


    /**
     * Registration view constructor.
     *
     * @param shell the app shell to navigate through once the search is submitted
     */
    public RegistrationView(AppShell shell) {
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
        Label subtitle = new Label("Crea un account gratuito. Bastano pochi secondi.");
        subtitle.getStyleClass().add(Styles.TEXT_CAPTION);
        subtitle.setStyle("-fx-text-fill: white;");
        subtitle.setWrapText(true);
        leftPanel.getChildren().addAll(topRow, spacer, title, subtitle);

        VBox rightPanel = new VBox(10);
        rightPanel.setStyle("-fx-background-color: #ffffff;");
        rightPanel.setPadding(new Insets(0, 100, 0, 100));
        rightPanel.setAlignment(Pos.CENTER_LEFT);

        Label greet = new Label("Crea il tuo account");
        greet.getStyleClass().add(Styles.TITLE_1);

        Label info = new Label("Scegli come vuoi usare TheKnife.");
        info.getStyleClass().add(Styles.TEXT_CAPTION);
        info.setStyle("-fx-text-fill: grey;");

        banner.setMaxWidth(Double.MAX_VALUE);

        HBox rowCard = new HBox(20);
        this.client = new RoleCard("Sono un cliente", "Prenota e recensisci", "fas-user");
        this.manager = new RoleCard("Sono un ristoratore", "Gestisci il tuo locale", "fas-store");
        rowCard.getChildren().addAll(client, manager);

        client.setOnMouseClicked(e -> {
            if (!client.getIsSelected()) {
                client.setSelected(true);
                manager.setSelected(false);
            }
        });
        manager.setOnMouseClicked(e -> {
            if (!manager.getIsSelected()) {
                client.setSelected(false);
                manager.setSelected(true);
            }
        });

        HBox nominativo = new HBox(20);
        Label name = new Label("Nome *");
        name.setStyle("-fx-text-fill: grey;");
        name.getStyleClass().add(Styles.TEXT_SMALL);
        nameField = new TextField();
        nameField.setPromptText("Mario");
        nameField.setMaxWidth(Double.MAX_VALUE);
        VBox nameBox = new VBox(name, nameField);
        Label surname = new Label("Cognome *");
        surname.setStyle("-fx-text-fill: grey;");
        surname.getStyleClass().add(Styles.TEXT_SMALL);
        surnameField = new TextField();
        surnameField.setPromptText("Rossi");
        surnameField.setMaxWidth(Double.MAX_VALUE);
        VBox surnameBox = new VBox(surname, surnameField);
        nominativo.setMaxWidth(Double.MAX_VALUE);
        nominativo.getChildren().addAll(nameBox, surnameBox);

        Label email = new Label("Email *");
        email.getStyleClass().add(Styles.TEXT_SMALL);
        email.setStyle("-fx-text-fill: grey;");
        emailField = new TextField();
        emailField.setPromptText("mario.rossi@gmail.com");
        emailField.setMaxWidth(Double.MAX_VALUE);

        Label place = new Label("Indirizzo *");
        place.setStyle("-fx-text-fill: grey;");
        place.getStyleClass().add(Styles.TEXT_SMALL);
        placeField = new TextField();
        placeField.setPromptText("Via Rossi 10, Milano");
        placeField.setMaxWidth(Double.MAX_VALUE);
        AddressAutocomplete.attach(placeField);

        Label dateBirth = new Label("Data di nascita (opzionale)");
        dateBirth.setStyle("-fx-text-fill: grey;");
        dateBirth.getStyleClass().add(Styles.TEXT_SMALL);
        dateBirthField = new DateSelector(null, date -> date.isAfter(LocalDate.now()));
        dateBirthField.setMaxWidth(Double.MAX_VALUE);

        HBox passwords = new HBox(20);
        Label password = new Label("Password *");
        password.setStyle("-fx-text-fill: grey;");
        password.getStyleClass().add(Styles.TEXT_SMALL);
        passwordField = new PasswordField();
        passwordField.setPromptText("••••••••••••");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        VBox passwordBox = new VBox(password, passwordField);
        Label confirm = new Label("Conferma password *");
        confirm.setStyle("-fx-text-fill: grey;");
        confirm.getStyleClass().add(Styles.TEXT_SMALL);
        confirmField = new PasswordField();
        confirmField.setPromptText("••••••••••••");
        confirmField.setMaxWidth(Double.MAX_VALUE);
        VBox confirmBox = new VBox(confirm, confirmField);
        passwords.setMaxWidth(Double.MAX_VALUE);
        passwords.getChildren().addAll(passwordBox, confirmBox);

        Button addUser = new Button("Crea account");
        addUser.setMaxWidth(Double.MAX_VALUE);
        addUser.setPrefHeight(40);
        addUser.getStyleClass().add(Styles.TEXT_NORMAL);
        addUser.setStyle("-fx-background-color: #000000; -fx-text-fill: white; ");
        addUser.setCursor(Cursor.HAND);
        addUser.setOnMouseClicked(e -> {
            banner.hide();
            this.handleRegistrationUser();
        });

        Text labelReg = new Text("Hai già un account? ");
        labelReg.getStyleClass().add(Styles.TEXT);
        Text reg = new Text("Accedi");
        reg.setFill(Color.GREEN);
        reg.setCursor(Cursor.HAND);
        reg.setOnMouseClicked(event -> shell.switchToLogin());

        HBox registerContainer = new HBox(new TextFlow(labelReg, reg));
        registerContainer.setAlignment(Pos.CENTER);

        rightPanel.getChildren().addAll(
                greet, info, banner, rowCard, nominativo,
                email, emailField,
                place, placeField,
                dateBirth, dateBirthField,
                passwords, addUser, registerContainer);

        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        HBox.setHgrow(surnameBox, Priority.ALWAYS);
        HBox.setHgrow(passwordBox, Priority.ALWAYS);
        HBox.setHgrow(confirmBox, Priority.ALWAYS);
        HBox.setHgrow(rowCard, Priority.ALWAYS);

        getChildren().addAll(leftPanel, rightPanel);
    }

    /**
     * Function to handle user registration: validates the form, creates the appropriate
     * {@link strazzullo.User} subtype, and calls the remote auth service.
     */
    private void handleRegistrationUser() {
        try {
            String name = nameField.getText(),
                    surname = surnameField.getText(),
                    email = emailField.getText(),
                    password = passwordField.getText(),
                    confirm = confirmField.getText(),
                    role = client.getIsSelected() ? "customer" : (manager.getIsSelected() ? "manager" : ""),
                    place = placeField.getText();

            LocalDate dateOfBirth = dateBirthField.getValue();

            if (place.isEmpty() || role.isEmpty() || name.isEmpty() || surname.isEmpty()
                    || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                banner.showError("Attenzione. Tutti i dati obbligatori devono essere compilati");
            } else {
                User u;
                if (dateOfBirth != null) {
                    u = role.equals("customer")
                            ? new Client(null, name, surname, email, password, place, formatDate(dateOfBirth))
                            : new Manager(null, name, surname, email, password, place, formatDate(dateOfBirth));
                } else {
                    u = role.equals("customer")
                            ? new Client(null, name, surname, email, password, place)
                            : new Manager(null, name, surname, email, password, place);
                }

                ServiceLocator.getInstance().getAuthService().register(u, password);
                nameField.clear();
                surnameField.clear();
                emailField.clear();
                placeField.clear();
                passwordField.clear();
                confirmField.clear();
                client.setSelected(false);
                manager.setSelected(false);
                banner.showSuccess("Registrazione avvenuta con successo");
            }
        } catch (RemoteException e) {
            banner.showError("Attenzione. Non è stato possibile proseguire con la registrazione. Controlla i dati inseriti.");
        }
    }

    /**
     * Function to format a {@link LocalDate} as {@code yyyy-MM-dd} for persistence.
     *
     * @param date the date to format
     * @return the formatted date string
     */
    private String formatDate(LocalDate date) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date);
    }
}
