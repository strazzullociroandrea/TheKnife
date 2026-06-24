package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Home screen: the TheKnife wordmark and a single search bar. Submitting it (via the search
 * button or Enter) navigates to {@link SearchView} with the typed city and query.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class HomeView extends VBox {

    /**
     * HomeView constructor.
     *
     * @param shell the app shell to navigate through once the search is submitted
     */
    public HomeView(AppShell shell) {
        setAlignment(Pos.CENTER);
        setSpacing(24);
        getStyleClass().add(Styles.BG_SUBTLE);

        Label logo = new Label("TheKnife");
        logo.getStyleClass().add(Styles.TITLE_1);

        Label tagline = new Label("Trova il ristorante giusto, ovunque tu sia.");
        tagline.getStyleClass().add(Styles.TEXT_MUTED);

        TextField cityField = new TextField();
        cityField.setPromptText("Città");
        cityField.setPrefWidth(180);

        TextField queryField = new TextField();
        queryField.setPromptText("Cerca ristoranti...");
        queryField.setPrefWidth(360);

        Button searchButton = new Button("", new FontIcon(Feather.SEARCH));
        searchButton.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.ACCENT);

        Runnable submit = () -> shell.showSearch(cityField.getText(), queryField.getText());
        searchButton.setOnAction(e -> submit.run());
        queryField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                submit.run();
            }
        });
        cityField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                submit.run();
            }
        });

        HBox searchBar = new HBox(8, new FontIcon(Feather.MAP_PIN), cityField, queryField, searchButton);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(10, 16, 10, 16));
        searchBar.getStyleClass().addAll(Styles.BG_DEFAULT, Styles.BORDER_MUTED, Styles.ROUNDED, Styles.ELEVATED_1);

        getChildren().addAll(logo, tagline, searchBar);
    }
}
