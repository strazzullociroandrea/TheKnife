package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.CategoryRow;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.CuisineFilterRow;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import sibilla.Cuisine;

import java.util.Collections;

/**
 * The home screen's body for guests and managers: a greeting, tagline, the cuisine quick-filter
 * row, and one {@link CategoryRow} per {@link Cuisine} (Glovo-style browsing), so the screen never
 * looks empty without a customer identity to personalize it. Every chip and "Vedi tutti" link
 * navigates straight into a filtered {@link SearchView}; every card's heart is auth-gated via
 * {@code onFavouriteAuthRequired}, prompting login/registration (or a role notice for managers)
 * instead of silently doing nothing.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class HomeGuestBody extends VBox {

    private static final int CATEGORY_ROW_SIZE = 6;

    /**
     * HomeGuestBody constructor. Builds the greeting, cuisine row, and every category row,
     * starting each row's own background fetch immediately.
     *
     * @param shell the app shell, used to open a location's detail screen and to navigate to
     *              filtered searches
     * @param onFavouriteAuthRequired callback invoked when a non-customer clicks any heart button
     */
    public HomeGuestBody(AppShell shell, Runnable onFavouriteAuthRequired) {
        Label greeting = new Label("Ciao, cosa mangiamo oggi?");
        greeting.getStyleClass().add(Styles.TITLE_1);

        Label tagline = new Label("Trova il ristorante giusto, ovunque tu sia.");
        tagline.getStyleClass().add(Styles.TEXT_MUTED);

        CuisineFilterRow cuisineRow = new CuisineFilterRow(
                cuisine -> shell.showSearch("", "", cuisine),
                () -> shell.showSearch("", ""));

        VBox categoriesBox = new VBox(28);
        for (Cuisine cuisine : Cuisine.values()) {
            categoriesBox.getChildren().add(new CategoryRow(cuisine, CATEGORY_ROW_SIZE,
                    shell::showLocationDetail, () -> shell.showSearch("", "", cuisine),
                    shell::isCustomer, Collections::emptySet, (result, nowFavourite) -> { }, onFavouriteAuthRequired));
        }

        setSpacing(20);
        getChildren().addAll(greeting, tagline, cuisineRow, categoriesBox);
        setPadding(new Insets(8, 32, 32, 32));
    }
}
