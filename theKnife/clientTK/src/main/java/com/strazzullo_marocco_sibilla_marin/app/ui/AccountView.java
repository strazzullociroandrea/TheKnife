package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.geo.AddressAutocomplete;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.DateSelector;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.MessageBanner;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import strazzullo.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Account screen showing the logged-in user's editable profile data (name, surname, email,
 * domicile, and date of birth) and a logout button. The domicile field uses {@link
 * AddressAutocomplete} for live Google/Photon suggestions, same as the registration form. Editing
 * a field and pressing "Salva modifiche" persists the change via {@code AuthService#updateProfile}
 * and refreshes both this screen's header and {@link AppShell}'s cached user. Also the destination
 * of the home screen's "cambia" domicile link, so changing where a customer lives happens on the
 * same profile screen as every other editable field, rather than a one-off dialog. Shares the same
 * two-panel layout as {@link LoginView}: a black left branding panel and a white right content
 * panel.
 *
 * @version 4.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class AccountView extends HBox {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AppShell shell;
    private final Label title;
    private final TextField nameField = new TextField();
    private final TextField surnameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField domicileField = new TextField();
    private final DateSelector dateOfBirthField;
    private final MessageBanner banner = new MessageBanner();

    /**
     * AccountView constructor.
     *
     * @param shell the app shell, used for navigation and to read/update the current user
     */
    public AccountView(AppShell shell) {
        this.shell = shell;
        User user = shell.getCurrentUser();
        String roleLabel = "manager".equals(user.getRole()) ? "Ristoratore" : "Cliente";

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

        title = new Label(user.getName() + " " + user.getSurname());
        title.getStyleClass().add(Styles.TITLE_1);
        title.setStyle("-fx-text-fill: white;");
        title.setWrapText(true);

        Label subtitle = new Label(roleLabel);
        subtitle.getStyleClass().add(Styles.TEXT_CAPTION);
        subtitle.setStyle("-fx-text-fill: white;");

        leftPanel.getChildren().addAll(topRow, spacer, title, subtitle);

        VBox rightPanel = new VBox(12);
        rightPanel.setStyle("-fx-background-color: #ffffff;");
        rightPanel.setPadding(new Insets(0, 100, 0, 100));
        rightPanel.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label("Il tuo profilo");
        heading.getStyleClass().add(Styles.TITLE_2);

        Separator sep = new Separator();
        sep.setMaxWidth(Double.MAX_VALUE);

        nameField.setText(user.getName());
        surnameField.setText(user.getSurname());
        emailField.setText(user.getEmail());
        domicileField.setText(user.getDomicile());
        AddressAutocomplete.attach(domicileField);
        dateOfBirthField = new DateSelector(parseDate(user.getDateOfBirth()), date -> date.isAfter(LocalDate.now()));
        dateOfBirthField.setMaxWidth(Double.MAX_VALUE);

        Button saveButton = new Button("Salva modifiche");
        HBox.setHgrow(saveButton, Priority.ALWAYS);
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setPrefHeight(40);
        saveButton.getStyleClass().add(Styles.ACCENT);
        saveButton.setCursor(Cursor.HAND);
        saveButton.setOnAction(e -> saveChanges());

        Button logoutButton = new Button("Esci", new FontIcon(Feather.LOG_OUT));
        logoutButton.setPrefHeight(40);
        logoutButton.getStyleClass().addAll(Styles.TEXT_NORMAL, Styles.DANGER, Styles.BUTTON_OUTLINED);
        logoutButton.setCursor(Cursor.HAND);
        logoutButton.setOnAction(e -> shell.logout());

        HBox actionsRow = new HBox(10, saveButton, logoutButton);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        rightPanel.getChildren().addAll(
                heading,
                sep,
                banner,
                buildField("Nome", nameField),
                buildField("Cognome", surnameField),
                buildField("Email", emailField),
                buildField("Indirizzo", domicileField),
                buildField("Data di nascita", dateOfBirthField),
                buildField("Ruolo", roleLabel),
                actionsRow
        );
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        getChildren().addAll(leftPanel, rightPanel);
    }

    /**
     * Function to persist the currently typed field values via {@code AuthService#updateProfile}
     * on a background thread, refreshing the header and {@link AppShell}'s cached user on
     * success, or showing an error banner on failure.
     */
    private void saveChanges() {
        User current = shell.getCurrentUser();
        String userId = current.getId();
        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String email = emailField.getText().trim();
        String domicile = domicileField.getText().trim();
        LocalDate dateOfBirth = dateOfBirthField.getValue();
        String dateOfBirthText = dateOfBirth == null ? null : DATE_FORMAT.format(dateOfBirth);

        Task<User> task = new Task<>() {
            @Override
            protected User call() throws Exception {
                return ServiceLocator.getInstance().getAuthService()
                        .updateProfile(userId, name, surname, email, domicile, dateOfBirthText);
            }
        };
        task.setOnSucceeded(e -> {
            shell.updateCurrentUser(task.getValue());
            title.setText(name + " " + surname);
            banner.showSuccess("Profilo aggiornato con successo.");
        });
        task.setOnFailed(e -> Platform.runLater(() ->
                banner.showError("Non è stato possibile aggiornare il profilo. Riprova più tardi.")));

        Thread thread = new Thread(task, "account-update-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to parse a "yyyy-MM-dd" date of birth string into a {@link LocalDate}.
     *
     * @param dateOfBirth the date of birth string, or null/blank
     * @return the parsed date, or null if blank or unparsable
     */
    private LocalDate parseDate(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateOfBirth, DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Builds a two-line editable field: a small grey label above the input control.
     *
     * @param labelText the field label, e.g. "Email"
     * @param control the input control to show below the label
     * @return a {@link VBox} containing the label and control
     */
    private VBox buildField(String labelText, javafx.scene.Node control) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add(Styles.TEXT_SMALL);
        lbl.setStyle("-fx-text-fill: grey;");

        return new VBox(2, lbl, control);
    }

    /**
     * Builds a two-line read-only field: a small grey label above the value text.
     *
     * @param labelText the field label, e.g. "Ruolo"
     * @param value the value to display
     * @return a {@link VBox} containing the label and value
     */
    private VBox buildField(String labelText, String value) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add(Styles.TEXT_SMALL);
        lbl.setStyle("-fx-text-fill: grey;");

        Label val = new Label(value != null ? value : "—");
        val.getStyleClass().add(Styles.TEXT_NORMAL);

        return new VBox(2, lbl, val);
    }
}
