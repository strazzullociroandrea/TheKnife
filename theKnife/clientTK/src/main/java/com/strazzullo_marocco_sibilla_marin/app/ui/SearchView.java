package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.geo.AddressGeocoder;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.search.ResultsSortOption;
import com.strazzullo_marocco_sibilla_marin.app.search.SearchCriteria;
import com.strazzullo_marocco_sibilla_marin.app.search.SearchFilterAssembler;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.CuisineFilterRow;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.DistanceFilterRow;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.FilterPanel;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.LocationPromptPanel;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.ResultCard;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import marocco.SearchFilter;
import sibilla.LocationSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Search results screen: search bar, quick cuisine filters, advanced {@link FilterPanel},
 * a results list, and a {@link MapView} showing the same results as pins. Selecting a result
 * focuses its pin on the map. The advanced filters and the "your position" prompt both open as
 * an in-app centered modal card (via {@link ModalPane}) instead of a separate OS dialog window,
 * so they stay visually part of this screen.
 * Building the {@link SearchFilter} actually sent to the server and choosing how to sort the
 * results are both delegated to the domain-level {@link SearchFilterAssembler} and {@link
 * ResultsSortOption}, so this class is left to coordinate its own UI components rather than also
 * encode those rules itself.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class SearchView extends StackPane {

    private final SearchToolbar searchToolbar;
    private final CuisineFilterRow cuisineFilterRow;
    private final DistanceFilterRow distanceFilterRow;
    private final Label resultsCountLabel = new Label();
    private final ComboBox<String> sortBox = new ComboBox<>();
    private final ListView<LocationSearchResult> resultsList = new ListView<>();
    private final MapView mapView = new MapView();
    private final ModalPane centeredModalPane = new ModalPane();

    private final ObservableList<LocationSearchResult> results = FXCollections.observableArrayList();
    private final AddressGeocoder geocoder = new AddressGeocoder();
    private AdvancedFilters advancedFilters = AdvancedFilters.empty();
    private String geocodedAddress;
    private AddressGeocoder.Coordinates geocodedCoordinates;

    /**
     * SearchView constructor. Builds the screen and runs an initial search.
     *
     * @param shell the app shell, used to navigate back to the home screen
     * @param city the initial city to search in, may be blank
     * @param query the initial free-text restaurant name query, may be blank
     */
    public SearchView(AppShell shell, String city, String query) {
        searchToolbar = new SearchToolbar(city, query, shell::showHome, this::runSearch);
        cuisineFilterRow = new CuisineFilterRow(this::onCuisineChanged, this::openFilterPanel);
        distanceFilterRow = new DistanceFilterRow(this::runSearch, this::openLocationPrompt);

        BorderPane content = new BorderPane();
        content.setTop(new VBox(searchToolbar, cuisineFilterRow, distanceFilterRow));
        content.setCenter(buildContent());

        centeredModalPane.setAlignment(Pos.CENTER);
        mapView.setOnLocateRequest(this::openLocationPrompt);

        getChildren().addAll(content, centeredModalPane);

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
        runSearch();
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
            runSearch();
        }, () -> centeredModalPane.hide(true));
        centeredModalPane.show(panel);
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
            runSearch();
        }, () -> centeredModalPane.hide(true));
        centeredModalPane.show(panel);
    }

    /**
     * Function to build the main content area: results list on the left, map on the right.
     *
     * @return the content split pane
     */
    private SplitPane buildContent() {
        for (ResultsSortOption option : ResultsSortOption.values()) {
            sortBox.getItems().add(option.label());
        }
        sortBox.getSelectionModel().selectFirst();
        sortBox.setOnAction(e -> applySort());

        HBox header = new HBox(resultsCountLabel, spacer(), new Label("Ordina"), sortBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(8);
        header.setPadding(new Insets(12, 16, 8, 16));

        resultsList.setItems(results);
        resultsList.getStyleClass().add(Styles.BG_SUBTLE);
        resultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(LocationSearchResult item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                getStyleClass().add("transparent-cell");
                setGraphic(empty || item == null ? null : new ResultCard(item));
                setPadding(new Insets(6, 10, 6, 10));
            }
        });
        resultsList.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                mapView.focusPin(newItem.location().getId());
            }
        });

        VBox left = new VBox(header, resultsList);
        left.getStyleClass().add(Styles.BG_SUBTLE);
        VBox.setVgrow(resultsList, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane(left, mapView);
        splitPane.setDividerPositions(0.45);
        return splitPane;
    }

    /**
     * Function to build a horizontally-growing spacer region.
     *
     * @return the spacer region
     */
    private Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    /**
     * Function to assemble the current screen state into a {@link SearchFilter} and run the
     * search on a background thread.
     */
    private void runSearch() {
        SearchFilter filter = SearchFilterAssembler.assemble(currentCriteria());
        updateRadiusReference();

        Task<List<LocationSearchResult>> task = new Task<>() {
            @Override
            protected List<LocationSearchResult> call() throws Exception {
                return ServiceLocator.getInstance().getCustomerService().searchLocations(filter);
            }
        };
        task.setOnSucceeded(e -> {
            results.setAll(task.getValue());
            applySort();
            updateMap();
            resultsCountLabel.setText(results.size() + " ristoranti trovati");
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
                searchToolbar.getCity(),
                searchToolbar.getQuery(),
                advancedFilters,
                distanceFilterRow.getAddress(),
                distanceFilterRow.getRadiusKm());
    }

    /**
     * Function to resolve the "Distanza" filter's reference address into coordinates and draw
     * its "you are here" pin and search radius circle on the map, or clear them if the filter
     * is not set. Runs the geocoding call on a background thread, mirroring the equivalent
     * lookup the server performs to evaluate the filter itself.
     */
    private void updateRadiusReference() {
        String address = distanceFilterRow.getAddress().isBlank() ? null : distanceFilterRow.getAddress().trim();
        double radiusKm = distanceFilterRow.getRadiusKm();
        if (address == null) {
            geocodedAddress = null;
            geocodedCoordinates = null;
            mapView.clearUserLocation();
            mapView.clearSearchRadius();
            return;
        }
        if (address.equals(geocodedAddress) && geocodedCoordinates != null) {
            mapView.setSearchRadius(geocodedCoordinates.lat(), geocodedCoordinates.lng(), radiusKm);
            return;
        }
        geocodedAddress = address;

        Task<Optional<AddressGeocoder.Coordinates>> task = new Task<>() {
            @Override
            protected Optional<AddressGeocoder.Coordinates> call() {
                return geocoder.geocode(address);
            }
        };
        task.setOnSucceeded(e -> task.getValue().ifPresent(coords -> {
            geocodedCoordinates = coords;
            mapView.setUserLocation(coords.lat(), coords.lng());
            mapView.setSearchRadius(coords.lat(), coords.lng(), radiusKm);
        }));

        Thread thread = new Thread(task, "geocode-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to re-sort the in-memory results according to the selected {@link ResultsSortOption}.
     */
    private void applySort() {
        ResultsSortOption option = ResultsSortOption.fromLabel(sortBox.getValue());
        if (option.comparator() != null) {
            results.sort(option.comparator());
        }
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
