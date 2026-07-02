package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.EmptyState;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.Pager;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.RestaurantTileCard;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.LocationSearchResult;

import java.util.List;

/**
 * Paginated screen listing every location a logged-in customer has favourited, most recently
 * added first. Reachable from {@link com.strazzullo_marocco_sibilla_marin.app.ui.components.HomeToolbar}'s
 * "Preferiti" button and the home screen's favourites row "Vedi tutti" link, via {@link
 * AppShell#showFavourites()}. Removing a heart on this screen re-loads the current page so a
 * freed slot backfills from the next page.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class FavouritesView extends StackPane {

    private static final int PAGE_SIZE = 8;

    private final AppShell shell;
    private final TilePane grid = new TilePane();
    private final Pager pager = new Pager(this::goToPrevPage, this::goToNextPage);
    private final EmptyState emptyState;
    private int currentPage = 0;

    /**
     * FavouritesView constructor. Builds the screen and loads the first page.
     *
     * @param shell the app shell, used to navigate back and to show a favourite's detail screen
     */
    public FavouritesView(AppShell shell) {
        this.shell = shell;

        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(4, 0, 4, 0));

        emptyState = new EmptyState(Feather.HEART, "Nessun preferito ancora",
                "Tocca il cuore su un ristorante per salvarlo qui e ritrovarlo velocemente.",
                "Esplora i ristoranti", () -> shell.showSearch(""));
        emptyState.setVisible(false);
        emptyState.setManaged(false);

        VBox content = new VBox(20, buildBreadcrumbRow(), buildTitle(), grid, emptyState, pager);
        content.setPadding(new Insets(20, 32, 32, 32));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add(Styles.BG_SUBTLE);

        getStyleClass().add(Styles.BG_SUBTLE);
        getChildren().add(scroll);

        loadPage();
    }

    /**
     * Function to build the "I tuoi preferiti" title.
     *
     * @return the title label
     */
    private Label buildTitle() {
        Label title = new Label("I tuoi preferiti", new FontIcon(Feather.HEART));
        title.getStyleClass().add(Styles.TITLE_2);
        return title;
    }

    /**
     * Function to build the top breadcrumb row: the TheKnife logo (navigates home) and an
     * "Indietro" button returning to wherever this screen was opened from.
     *
     * @return the breadcrumb row
     */
    private HBox buildBreadcrumbRow() {
        Label logo = new Label("TheKnife");
        logo.getStyleClass().add(Styles.TEXT_BOLD);
        logo.setOnMouseClicked(e -> shell.showHome());
        logo.setCursor(Cursor.HAND);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("Indietro", new FontIcon(Feather.ARROW_LEFT));
        backButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        backButton.setOnAction(e -> shell.goBack());

        HBox row = new HBox(logo, spacer, backButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Function to go back one page, if not already on the first, and reload.
     */
    private void goToPrevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadPage();
        }
    }

    /**
     * Function to advance to the next page and reload.
     */
    private void goToNextPage() {
        currentPage++;
        loadPage();
    }

    /**
     * Function to load the current page of favourites in the background and render it.
     */
    private void loadPage() {
        String userId = shell.getCurrentUserId();
        int page = currentPage;
        Task<List<LocationSearchResult>> task = new Task<>() {
            @Override
            protected List<LocationSearchResult> call() throws Exception {
                return ServiceLocator.getInstance().getFavouriteService().listFavourites(userId, page, PAGE_SIZE + 1);
            }
        };
        task.setOnSucceeded(e -> {
            List<LocationSearchResult> raw = task.getValue();
            boolean hasNext = raw.size() > PAGE_SIZE;
            render(hasNext ? raw.subList(0, PAGE_SIZE) : raw);
            pager.setState(currentPage, currentPage > 0, hasNext);
        });
        Thread thread = new Thread(task, "favourites-page-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to render a page's favourites as tile cards, showing the empty-state message
     * instead when the very first page has none.
     *
     * @param items the page's favourites
     */
    private void render(List<LocationSearchResult> items) {
        grid.getChildren().clear();
        for (LocationSearchResult result : items) {
            grid.getChildren().add(new RestaurantTileCard(result, true, true,
                    () -> shell.showLocationDetail(result),
                    nowFavourite -> removeFavourite(result)));
        }

        boolean empty = items.isEmpty() && currentPage == 0;
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
        pager.setVisible(!empty);
        pager.setManaged(!empty);
    }

    /**
     * Function to remove a location from the current customer's favourites and reload the
     * current page, so a freed slot backfills from the next page.
     *
     * @param result the location to remove
     */
    private void removeFavourite(LocationSearchResult result) {
        String userId = shell.getCurrentUserId();
        String locationId = result.location().getId();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ServiceLocator.getInstance().getFavouriteService().removeFavourite(userId, locationId);
                return null;
            }
        };
        task.setOnSucceeded(e -> loadPage());
        Thread thread = new Thread(task, "favourite-remove-worker");
        thread.setDaemon(true);
        thread.start();
    }
}
