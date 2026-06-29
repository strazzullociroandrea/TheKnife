package com.strazzullo_marocco_sibilla_marin.app.ui;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import sibilla.LocationSearchResult;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Root container of the client UI, swapping its single child between screens. The home screen
 * and search results are entry points that reset navigation; sub-pages reached from them (the
 * location detail, login, and registration screens) push onto a small back-stack instead, so
 * {@link #goBack()} always returns to wherever the user came from.
 *
 * @version 3.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class AppShell extends StackPane {

    private final Deque<Node> backStack = new ArrayDeque<>();
    private String currentUserId = null;

    /**
     * AppShell constructor. Starts on the home screen.
     */
    public AppShell() {
        showHome();
    }

    /**
     * Function to check whether a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    /**
     * Function to resolve the id of the currently logged-in user. Returns {@code null} when no
     * user is logged in.
     *
     * @return the current user's id, or {@code null} if not logged in
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Function to record which user just logged in.
     *
     * @param id the logged-in user's id
     */
    public void setCurrentUserId(String id) {
        this.currentUserId = id;
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
     * @param city  the city to search in, may be blank
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

    /**
     * Function to navigate to the login screen, pushing the current screen onto the back-stack so
     * {@link #goBack()} returns to it after login.
     */
    public void showLogin() {
        pushCurrent();
        show(new LoginView(this));
    }

    /**
     * Function to navigate to the registration screen, pushing the current screen onto the
     * back-stack so {@link #goBack()} returns to it.
     */
    public void showRegistrationView() {
        pushCurrent();
        show(new RegistrationView(this));
    }

    /**
     * Function to replace the current screen with the login screen without touching the back-stack.
     * Used after registration so the pre-registration back-stack entry is preserved for after login.
     */
    public void switchToLogin() {
        show(new LoginView(this));
    }
}
