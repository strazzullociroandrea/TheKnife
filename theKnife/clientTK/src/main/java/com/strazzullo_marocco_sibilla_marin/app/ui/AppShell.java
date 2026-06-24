package com.strazzullo_marocco_sibilla_marin.app.ui;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import sibilla.LocationSearchResult;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Root container of the client UI, swapping its single child between screens. The home screen
 * and search results are entry points that reset navigation; sub-pages reached from them (the
 * location detail screen, and whatever else needs a "back" button in the future) push onto a
 * small back-stack instead, so {@link #goBack()} always returns to wherever the user came from.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class AppShell extends StackPane {

    /**
     * Stand-in for the logged-in user's id until a real login screen is wired up to {@code
     * AuthService}. Booking has no login gate yet by design (it will once login exists), so
     * every booking made through this build is attributed to this seeded demo customer.
     */
    private static final String DEMO_USER_ID = "11111111-1111-1111-1111-111111111101";

    private final Deque<Node> backStack = new ArrayDeque<>();
    private String currentUserId = DEMO_USER_ID;

    /**
     * AppShell constructor. Starts on the home screen.
     */
    public AppShell() {
        showHome();
    }

    /**
     * Function to resolve the id of the user currently "logged in", for screens (like booking)
     * that need one. Always the same demo customer until a real login screen exists.
     *
     * @return the current user's id
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Function to navigate to the home screen, resetting any back-stack built up so far.
     */
    public void showHome() {
        backStack.clear();
        show(new HomeView(this));
    }

    /**
     * Function to navigate to the search results screen, running an initial search and resetting
     * any back-stack built up so far.
     *
     * @param city the city to search in, may be blank
     * @param query the free-text restaurant name query, may be blank
     */
    public void showSearch(String city, String query) {
        backStack.clear();
        show(new SearchView(this, city, query));
    }

    /**
     * Function to navigate to a location's detail screen, pushing the current screen onto the
     * back-stack so {@link #goBack()} returns to it.
     *
     * @param result the location (plus its restaurant/rating info) to show
     */
    public void showLocationDetail(LocationSearchResult result) {
        pushCurrent();
        show(new LocationDetailView(this, result));
    }

    /**
     * Function to return to whichever screen was showing before the current one was pushed onto
     * the back-stack, or the home screen if the stack is empty.
     */
    public void goBack() {
        if (backStack.isEmpty()) {
            showHome();
        } else {
            show(backStack.pop());
        }
    }

    private void pushCurrent() {
        if (!getChildren().isEmpty()) {
            backStack.push(getChildren().get(0));
        }
    }

    private void show(Node view) {
        getChildren().setAll(view);
    }
}
