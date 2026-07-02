package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import marocco.SearchFilter;
import sibilla.Cuisine;
import sibilla.LocationSearchResult;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A single Glovo-style browsing row for one {@link Cuisine}: a header (the cuisine's name plus a
 * "Vedi tutti" link) and a horizontal row of {@link RestaurantTileCard}s, fetched in the
 * background and hidden entirely if the cuisine has no locations to show. Shared by the home
 * screen's guest browsing rows and its "Consigliati per te" fallback (when a logged-in customer
 * has no personalized recommendations yet), so both read identically and neither duplicates the
 * fetch/render logic.
 * Takes only narrow callbacks rather than the {@code AppShell} itself, matching {@link
 * ResultCard} and {@link RestaurantTileCard}'s convention of keeping presentational components
 * decoupled from navigation.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class CategoryRow extends VBox {

    private final HBox row = new HBox(16);

    /**
     * CategoryRow constructor. Builds the row (initially hidden) and starts its background fetch.
     *
     * @param cuisine the cuisine this row browses
     * @param rowSize the maximum number of locations to show
     * @param onViewDetails callback invoked with a result when its card is clicked
     * @param onViewAll callback invoked when the "Vedi tutti" link is clicked
     * @param isCustomer supplier returning whether the current user is a logged-in customer
     * @param favouriteIds supplier returning the ids of the current user's favourites, consulted
     *                     once per card at render time
     * @param onFavouriteToggle callback invoked with a result and the heart's new state when a
     *                          customer toggles it
     * @param onFavouriteAuthRequired callback invoked when a non-customer clicks a heart
     */
    public CategoryRow(Cuisine cuisine, int rowSize, Consumer<LocationSearchResult> onViewDetails, Runnable onViewAll,
                        BooleanSupplier isCustomer, Supplier<Set<String>> favouriteIds,
                        BiConsumer<LocationSearchResult, Boolean> onFavouriteToggle, Runnable onFavouriteAuthRequired) {
        Label title = new Label(CuisineLabels.of(cuisine));
        title.getStyleClass().add(Styles.TITLE_3);

        Label viewAllLink = new Label("Vedi tutti");
        viewAllLink.getStyleClass().add("tk-link");
        viewAllLink.setCursor(Cursor.HAND);
        viewAllLink.setOnMouseClicked(e -> onViewAll.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, title, spacer, viewAllLink);
        header.setAlignment(Pos.CENTER_LEFT);

        row.setAlignment(Pos.CENTER_LEFT);

        setSpacing(12);
        getChildren().addAll(header, row);
        setVisible(false);
        setManaged(false);

        load(cuisine, rowSize, onViewDetails, isCustomer, favouriteIds, onFavouriteToggle, onFavouriteAuthRequired);
    }

    /**
     * Function to fetch the cuisine's top-rated locations in the background and render them as
     * tile cards, hiding the whole row if there are none.
     *
     * @param cuisine the cuisine to fetch
     * @param rowSize the maximum number of locations to show
     * @param onViewDetails callback invoked with a result when its card is clicked
     * @param isCustomer supplier returning whether the current user is a logged-in customer
     * @param favouriteIds supplier returning the ids of the current user's favourites
     * @param onFavouriteToggle callback invoked with a result and the heart's new state
     * @param onFavouriteAuthRequired callback invoked when a non-customer clicks a heart
     */
    private void load(Cuisine cuisine, int rowSize, Consumer<LocationSearchResult> onViewDetails,
                       BooleanSupplier isCustomer, Supplier<Set<String>> favouriteIds,
                       BiConsumer<LocationSearchResult, Boolean> onFavouriteToggle, Runnable onFavouriteAuthRequired) {
        SearchFilter filter = new SearchFilter.Builder().cuisineType(cuisine).page(0, rowSize).build();
        Task<List<LocationSearchResult>> task = new Task<>() {
            @Override
            protected List<LocationSearchResult> call() throws Exception {
                return ServiceLocator.getInstance().getCustomerService().searchLocations(filter);
            }
        };
        task.setOnSucceeded(e -> {
            List<LocationSearchResult> results = task.getValue();
            row.getChildren().clear();
            for (LocationSearchResult result : results) {
                row.getChildren().add(new RestaurantTileCard(result, true,
                        favouriteIds.get().contains(result.location().getId()),
                        () -> onViewDetails.accept(result), nowFavourite -> onFavouriteToggle.accept(result, nowFavourite),
                        isCustomer, onFavouriteAuthRequired));
            }
            boolean empty = results.isEmpty();
            setVisible(!empty);
            setManaged(!empty);
        });
        Thread thread = new Thread(task, "category-row-" + cuisine.name() + "-worker");
        thread.setDaemon(true);
        thread.start();
    }
}
