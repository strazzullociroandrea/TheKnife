package com.strazzullo_marocco_sibilla_marin.app.ui;

import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.session.SessionStore;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import sibilla.LocationSearchResult;
import strazzullo.Client;
import strazzullo.User;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Root container of the client UI, swapping its single child between screens. The home screen
 * and search results are entry points that reset navigation; sub-pages reached from them (the
 * location detail, login, and registration screens) push onto a small back-stack instead, so
 * {@link #goBack()} always returns to wherever the user came from.
 * On startup the shell attempts to restore a previous session from disk so the user does not have
 * to log in again after restarting the app.
 *
 * @version 5.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class AppShell extends StackPane {

    private final Deque<Node> backStack = new ArrayDeque<>();
    private User currentUser = null;
    private String currentSessionToken = null;

    /**
     * AppShell constructor. Attempts to restore a previous session, then shows the home screen.
     */
    public AppShell() {
        tryRestoreSession();
        showHome();
    }

    /**
     * Function to check whether a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Function to resolve the id of the currently logged-in user. Returns {@code null} when no
     * user is logged in.
     *
     * @return the current user's id, or {@code null} if not logged in
     */
    public String getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * Function to check whether the currently logged-in user is a customer. Returns {@code false}
     * when no user is logged in or the user is a manager.
     *
     * @return true if a customer is logged in, false otherwise
     */
    public boolean isCustomer() {
        return currentUser instanceof Client;
    }

    /**
     * Function to resolve the currently logged-in user. Returns {@code null} when no user is
     * logged in.
     *
     * @return the current user, or {@code null} if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Records the authenticated user and its session token. Called by {@link LoginView} after a
     * successful login RMI call.
     *
     * @param user  the authenticated user
     * @param token the session token returned by the server
     */
    public void setSession(User user, String token) {
        this.currentUser = user;
        this.currentSessionToken = token;
    }

    /**
     * Function to log out the current user: invalidates the session on the server, removes the
     * token from disk, and returns to the home screen.
     */
    public void logout() {
        if (currentSessionToken != null) {
            try {
                ServiceLocator.getInstance().getAuthService().logout(currentSessionToken);
            } catch (Exception ignored) {
            }
            SessionStore.clear();
            currentSessionToken = null;
        }
        currentUser = null;
        showHome();
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

    /**
     * Saves the currently displayed screen onto the back-stack so it can be restored by
     * {@link #goBack()}. No-op if no screen is currently shown.
     */
    private void pushCurrent() {
        if (!getChildren().isEmpty()) {
            backStack.push(getChildren().get(0));
        }
    }

    /**
     * Replaces the shell's single child with the given view, making it the active screen.
     *
     * @param view the screen node to display
     */
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
     * Function to navigate to the account screen if logged in, or the login screen otherwise,
     * pushing the current screen onto the back-stack.
     */
    public void showAccountOrLogin() {
        if (isLoggedIn()) {
            pushCurrent();
            show(new AccountView(this));
        } else {
            showLogin();
        }
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
     * Function to replace the current screen with the login screen, popping one level from the
     * back-stack first. Used after registration so {@link #goBack()} after login returns to the
     * screen that was showing before the whole auth flow started.
     */
    public void switchToLogin() {
        if (!backStack.isEmpty()) backStack.pop();
        show(new LoginView(this));
    }

    /**
     * Reads the persisted session token from {@link SessionStore} and, if found, validates it
     * against the server. On success the current user and token are set in memory; on failure the
     * stale token is deleted from disk so the next startup skips validation.
     */
    private void tryRestoreSession() {
        SessionStore.load().ifPresent(token -> {
            try {
                User user = ServiceLocator.getInstance().getAuthService().validateSession(token);
                if (user != null) {
                    currentUser = user;
                    currentSessionToken = token;
                } else {
                    SessionStore.clear();
                }
            } catch (Exception ignored) {
                SessionStore.clear();
            }
        });
    }
}
