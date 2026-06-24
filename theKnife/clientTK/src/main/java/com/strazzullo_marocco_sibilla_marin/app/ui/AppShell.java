package com.strazzullo_marocco_sibilla_marin.app.ui;

import javafx.scene.layout.StackPane;

/**
 * Root container of the client UI, swapping its single child between the home screen and the
 * search results screen. Kept deliberately simple: there is no back-stack, just two screens.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class AppShell extends StackPane {

    /**
     * AppShell constructor. Starts on the home screen.
     */
    public AppShell() {
        showHome();
    }

    /**
     * Function to navigate to the home screen.
     */
    public void showHome() {
        getChildren().setAll(new HomeView(this));
    }

    /**
     * Function to navigate to the search results screen, running an initial search.
     *
     * @param city the city to search in, may be blank
     * @param query the free-text restaurant name query, may be blank
     */
    public void showSearch(String city, String query) {
        getChildren().setAll(new SearchView(this, city, query));
    }
}
