package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.AuthPromptCard;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.HomeToolbar;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Home screen, with two variants sharing the same {@link HomeToolbar}:
 * <ul>
 *   <li>Guest/manager: delegates its body to {@link HomeGuestBody} — a greeting, tagline, and
 *       one Glovo-style browsing row per cuisine, since no personalized section needs a customer
 *       identity.</li>
 *   <li>Logged-in customer: delegates its body to {@link HomeCustomerBody} — a location-aware
 *       greeting, a "next booking" banner, a favourites preview, and a paginated "Consigliati per
 *       te" grid.</li>
 * </ul>
 * This class itself only owns the shared {@link HomeToolbar} and {@link ModalPane}, decides which
 * body to build, and hosts the guest body's favourite-auth prompt (the customer body hosts its
 * own dialogs directly since it already receives the same modal pane). Submitting the toolbar's
 * search pill (via the search button or Enter) navigates to {@link SearchView} with the typed
 * city and query.
 *
 * @version 3.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class HomeView extends StackPane {

    private final AppShell shell;
    private final ModalPane modalPane = new ModalPane();
    private final HomeToolbar toolbar;

    /**
     * HomeView constructor.
     *
     * @param shell the app shell to navigate through
     */
    public HomeView(AppShell shell) {
        this.shell = shell;
        getStyleClass().add(Styles.BG_SUBTLE);

        boolean loggedIn = shell.isLoggedIn();
        boolean showCustomerButtons = loggedIn && shell.isCustomer();
        toolbar = new HomeToolbar(this::onSearchFromToolbar, shell::showAccountOrLogin,
                loggedIn, showCustomerButtons, shell::showBookings, shell::showFavourites);

        VBox body = showCustomerButtons
                ? new HomeCustomerBody(shell, modalPane)
                : new HomeGuestBody(shell, this::promptFavouriteAuth);

        VBox root = new VBox(24, toolbar, body);
        root.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add(Styles.BG_SUBTLE);

        modalPane.setAlignment(Pos.CENTER);
        getChildren().addAll(scroll, modalPane);
    }

    /**
     * Function to navigate to the search results screen with the toolbar's currently typed query.
     */
    private void onSearchFromToolbar() {
        shell.showSearch(toolbar.getQuery());
    }

    /**
     * Function to show the same favourite-auth prompt {@link SearchView} shows: a role notice for
     * logged-in managers, or login/registration actions for guests. Only reachable from {@link
     * HomeGuestBody}'s category rows, since the customer body's cards are never auth-gated.
     */
    private void promptFavouriteAuth() {
        if (shell.isLoggedIn()) {
            modalPane.show(new AuthPromptCard(
                    "I preferiti sono disponibili solo per i clienti.",
                    () -> modalPane.hide(true)));
        } else {
            modalPane.show(new AuthPromptCard(
                    "Per aggiungere ai preferiti, accedi o registrati.",
                    () -> modalPane.hide(true),
                    shell::showLogin,
                    shell::showRegistrationView));
        }
    }
}
