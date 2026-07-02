package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.geo.AddressGeocoder;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.search.SearchCriteria;
import com.strazzullo_marocco_sibilla_marin.app.search.SearchFilterAssembler;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.CuisineFilterRow;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.DistanceFilterRow;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.AuthPromptCard;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.FilterPanel;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.LocationPromptPanel;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.Pager;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.SearchResultsPanel;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.SearchToolbar;
import com.strazzullo_marocco_sibilla_marin.app.ui.map.MapPin;
import com.strazzullo_marocco_sibilla_marin.app.ui.map.MapView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import marocco.SearchFilter;
import sibilla.Cuisine;
import sibilla.LocationSearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Search results screen: search bar, quick cuisine filters, advanced {@link FilterPanel}, a
 * {@link SearchResultsPanel}, and a {@link MapView} showing the same results as pins. Selecting a
 * result focuses its pin on the map. The advanced filters and the "your position" prompt both
 * open as an in-app centered modal card (via {@link ModalPane}) instead of a separate OS dialog
 * window, so they stay visually part of this screen.
 * Building the {@link SearchFilter} actually sent to the server is delegated to the domain-level
 * {@link SearchFilterAssembler}, so this class is left to coordinate its own UI components rather
 * than also encode that rule itself.
 *
 * @version 4.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class SearchView extends StackPane {

    private static final int PAGE_SIZE = 20;

    private final SearchToolbar searchToolbar;
    private final CuisineFilterRow cuisineFilterRow;
    private final DistanceFilterRow distanceFilterRow;
    private final MapView mapView = new MapView();
    private final ModalPane centeredModalPane = new ModalPane();
    private final Pager pager = new Pager(this::goToPrevPage, this::goToNextPage);
    private SearchResultsPanel resultsPanel;

    private final ObservableList<LocationSearchResult> results = FXCollections.observableArrayList();
    private final AddressGeocoder geocoder = new AddressGeocoder();
    private final SearchDistanceReference distanceReference = new SearchDistanceReference(geocoder, mapView);
    private final AppShell shell;
    private AdvancedFilters advancedFilters = AdvancedFilters.empty();
    private int currentPage = 0;

    /**
     * SearchView constructor. Builds the screen and runs an initial search.
     *
     * @param shell the app shell, used to navigate back to the home screen
     * @param query the initial free-text query, may be blank
     */
    public SearchView(AppShell shell, String query) {
        this(shell, query, null);
    }

    /**
     * SearchView constructor with a cuisine already applied, used by the home screen's cuisine
     * quick-filter chips to jump straight into a filtered search.
     *
     * @param shell the app shell, used to navigate back to the home screen
     * @param query the initial free-text query, may be blank
     * @param initialCuisine the cuisine to pre-select, or null for none
     */
    public SearchView(AppShell shell, String query, Cuisine initialCuisine) {
        this(shell, query, initialCuisine, null);
    }

    /**
     * SearchView constructor with a cuisine and/or a "Distanza da un indirizzo" reference address
     * already applied, used by the home screen's customer dashboard so its resolved position
     * (from "cambia") feeds the distance filter rather than the plain free-text search field,
     * which is a separate, general name/city/address query.
     *
     * @param shell the app shell, used to navigate back to the home screen
     * @param query the initial free-text query, may be blank
     * @param initialCuisine the cuisine to pre-select, or null for none
     * @param initialDistanceAddress the "Distanza da un indirizzo" reference address to pre-fill,
     *                               or null/blank for none
     */
    public SearchView(AppShell shell, String query, Cuisine initialCuisine, String initialDistanceAddress) {
        this.shell = shell;
        searchToolbar = new SearchToolbar(query, shell::showHome, this::runSearchResetPage,
                shell::showAccountOrLogin, shell.isLoggedIn());
        cuisineFilterRow = new CuisineFilterRow(this::onCuisineChanged, this::openFilterPanel);
        distanceFilterRow = new DistanceFilterRow(this::runSearchResetPage, this::openLocationPrompt);

        if (initialCuisine != null) {
            advancedFilters = advancedFilters.withCuisineType(initialCuisine);
            cuisineFilterRow.syncSelection(initialCuisine);
        }
        if (initialDistanceAddress != null && !initialDistanceAddress.isBlank()) {
            distanceFilterRow.setAddress(initialDistanceAddress);
        }

        BorderPane content = new BorderPane();
        content.setTop(new VBox(searchToolbar, cuisineFilterRow, distanceFilterRow));
        content.setCenter(buildContent());

        centeredModalPane.setAlignment(Pos.CENTER);
        mapView.setOnLocateRequest(this::openLocationPrompt);
        mapView.setOnPinDetails(this::openPinDetails);

        getChildren().addAll(content, centeredModalPane);

        runSearch();
    }

    /**
     * Function to reset back to the first page and re-run the search, used whenever the search
     * criteria themselves change (as opposed to just turning a pager page).
     */
    private void runSearchResetPage() {
        currentPage = 0;
        runSearch();
    }

    /**
     * Function to go back one page, if not already on the first, and re-run the search.
     */
    private void goToPrevPage() {
        if (currentPage > 0) {
            currentPage--;
            runSearch();
        }
    }

    /**
     * Function to advance to the next page and re-run the search.
     */
    private void goToNextPage() {
        currentPage++;
        runSearch();
    }

    /**
     * Function to apply a cuisine picked from {@link #cuisineFilterRow} to {@link
     * #advancedFilters} and re-run the search.
     *
     * @param cuisine the newly selected cuisine, or null if deselected
     */
    private void onCuisineChanged(sibilla.Cuisine cuisine) {
        advancedFilters = advancedFilters.withCuisineType(cuisine);
        runSearchResetPage();
    }

    /**
     * Function to open the advanced filters panel as an in-app centered modal card and re-run the
     * search if its changes are applied.
     */
    private void openFilterPanel() {
        FilterPanel panel = new FilterPanel(advancedFilters, filters -> {
            advancedFilters = filters;
            cuisineFilterRow.setFiltersActive(!advancedFilters.isEmpty());
            cuisineFilterRow.syncSelection(advancedFilters.cuisineType());
            centeredModalPane.hide(true);
            runSearchResetPage();
        }, () -> centeredModalPane.hide(true));
        centeredModalPane.show(panel);
    }

    /**
     * Function to open a location's detail screen from the map pin popup's "Dettagli" button.
     *
     * @param locationId the id of the pin's location, matched against the current results
     */
    private void openPinDetails(String locationId) {
        results.stream()
                .filter(result -> result.location().getId().equals(locationId))
                .findFirst()
                .ifPresent(shell::showLocationDetail);
    }

    /**
     * Function to open the "your position" prompt as an in-app centered modal card, used to
     * resolve the "Distanza" filter's reference address either automatically (preferring a
     * Wi-Fi scan, falling back to the user's IP) or manually, when the map's locate-me button is
     * pressed.
     */
    private void openLocationPrompt() {
        LocationPromptPanel panel = new LocationPromptPanel(geocoder, address -> {
            distanceFilterRow.setAddress(address);
            centeredModalPane.hide(true);
            runSearchResetPage();
        }, () -> centeredModalPane.hide(true));
        centeredModalPane.show(panel);
    }

    /**
     * Function to build the main content area: results list (with its pager) on the left, map on
     * the right.
     *
     * @return the content area
     */
    private VBox buildContent() {
        resultsPanel = new SearchResultsPanel(results,
                newItem -> mapView.focusPin(newItem.location().getId()), shell::showLocationDetail,
                shell::isCustomer,
                () -> {
                    if (shell.isLoggedIn()) {
                        centeredModalPane.show(new AuthPromptCard(
                                "I preferiti sono disponibili solo per i clienti.",
                                () -> centeredModalPane.hide(true)));
                    } else {
                        centeredModalPane.show(new AuthPromptCard(
                                "Per aggiungere ai preferiti, accedi o registrati.",
                                () -> centeredModalPane.hide(true),
                                shell::showLogin,
                                shell::showRegistrationView));
                    }
                },
                this::persistFavourite);

        SplitPane splitPane = new SplitPane(resultsPanel, mapView);
        splitPane.setDividerPositions(0.45);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        return new VBox(splitPane, pager);
    }

    /**
     * Function to persist a favourite toggle fired from a result card, on a background thread.
     * The card's own selection state has already been updated optimistically by the time this runs.
     *
     * @param result the location whose favourite state changed
     * @param nowFavourite the heart's new state
     */
    private void persistFavourite(LocationSearchResult result, boolean nowFavourite) {
        String userId = shell.getCurrentUserId();
        String locationId = result.location().getId();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (nowFavourite) {
                    ServiceLocator.getInstance().getFavouriteService().addFavourite(userId, locationId);
                } else {
                    ServiceLocator.getInstance().getFavouriteService().removeFavourite(userId, locationId);
                }
                return null;
            }
        };
        Thread thread = new Thread(task, "favourite-toggle-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to load the current customer's favourite location ids in the background and hand
     * them to {@link #resultsPanel}, so every visible card's heart reflects real persisted state.
     * A no-op (clearing the set) for guests and managers.
     */
    private void loadFavourites() {
        if (!shell.isCustomer()) {
            resultsPanel.setFavouriteIds(Collections.emptySet());
            return;
        }
        String userId = shell.getCurrentUserId();
        Task<Set<String>> task = new Task<>() {
            @Override
            protected Set<String> call() throws Exception {
                return ServiceLocator.getInstance().getFavouriteService().listFavouriteLocationIds(userId);
            }
        };
        task.setOnSucceeded(e -> resultsPanel.setFavouriteIds(task.getValue()));
        Thread thread = new Thread(task, "favourites-load-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to assemble the current screen state into a {@link SearchFilter} and run the
     * search on a background thread.
     */
    private void runSearch() {
        SearchFilter filter = SearchFilterAssembler.assemble(currentCriteria(), currentPage, PAGE_SIZE + 1);
        distanceReference.update(distanceFilterRow.getAddress(), distanceFilterRow.getRadiusKm());

        Task<List<LocationSearchResult>> task = new Task<>() {
            @Override
            protected List<LocationSearchResult> call() throws Exception {
                return ServiceLocator.getInstance().getCustomerService().searchLocations(filter);
            }
        };
        task.setOnSucceeded(e -> {
            List<LocationSearchResult> raw = task.getValue();
            boolean hasNext = raw.size() > PAGE_SIZE;
            results.setAll(hasNext ? raw.subList(0, PAGE_SIZE) : raw);
            resultsPanel.refresh();
            pager.setState(currentPage, currentPage > 0, hasNext);
            updateMap();
            loadFavourites();
        });
        task.setOnFailed(e -> showError(task.getException()));

        Thread thread = new Thread(task, "search-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to snapshot the screen's current criteria into a {@link SearchCriteria}.
     *
     * @return the current criteria
     */
    private SearchCriteria currentCriteria() {
        return new SearchCriteria(
                searchToolbar.getQuery(),
                advancedFilters,
                distanceFilterRow.getAddress(),
                distanceFilterRow.getRadiusKm());
    }

    /**
     * Function to push the current results onto the map as pins.
     */
    private void updateMap() {
        List<MapPin> pins = new ArrayList<>();
        for (LocationSearchResult result : results) {
            sibilla.Location location = result.location();
            if (location.getLatitude() != null && location.getLongitude() != null) {
                pins.add(new MapPin(location.getId(), location.getLatitude(), location.getLongitude(),
                        result.restaurantName(), location.getAddress() + ", " + location.getCity()));
            }
        }
        mapView.setPins(pins);
    }

    /**
     * Function to surface a search failure to the user.
     *
     * @param error the error raised while searching
     */
    private void showError(Throwable error) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ricerca non riuscita: " + error.getMessage());
            alert.showAndWait();
        });
    }
}
