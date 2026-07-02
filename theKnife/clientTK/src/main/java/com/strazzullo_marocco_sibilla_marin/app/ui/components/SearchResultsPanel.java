package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.search.ResultsSortOption;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import sibilla.LocationSearchResult;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * The search screen's results column: a "n ristoranti trovati" count plus a sort picker, and the
 * scrollable list of {@link ResultCard}s itself. Owns sorting the shared results list in place,
 * so {@link com.strazzullo_marocco_sibilla_marin.app.ui.SearchView} only has to call {@link
 * #refresh()} after a search completes. Also owns which locations are currently favourited, so a
 * toggle's effect survives a re-sort or scroll-triggered cell rebuild within this screen.
 *
 * @version 3.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class SearchResultsPanel extends VBox {

    private final Label countLabel = new Label();
    private final ComboBox<String> sortBox = new ComboBox<>();
    private final ListView<LocationSearchResult> listView = new ListView<>();
    private final ObservableList<LocationSearchResult> results;
    private final Set<String> favouriteIds = new HashSet<>();

    /**
     * @param results the shared, mutable results list to display and sort
     * @param onSelected called with the newly selected result, e.g. to focus its map pin
     * @param onViewDetails called with a result when its card is clicked
     * @param isCustomer supplier returning whether the current user is a logged-in customer
     * @param onFavouriteAuthRequired called when a non-customer clicks the heart button on any card
     * @param onFavouriteToggle called with a result and its new favourite state when a customer
     *                          toggles its heart, so the host screen can persist it
     */
    public SearchResultsPanel(ObservableList<LocationSearchResult> results,
                               Consumer<LocationSearchResult> onSelected, Consumer<LocationSearchResult> onViewDetails,
                               BooleanSupplier isCustomer, Runnable onFavouriteAuthRequired,
                               BiConsumer<LocationSearchResult, Boolean> onFavouriteToggle) {
        this.results = results;

        for (ResultsSortOption option : ResultsSortOption.values()) {
            sortBox.getItems().add(option.label());
        }
        sortBox.getSelectionModel().selectFirst();
        sortBox.setOnAction(e -> resort());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, countLabel, spacer, new Label("Ordina"), sortBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 8, 16));

        listView.setItems(results);
        listView.getStyleClass().add(Styles.BG_SUBTLE);
        listView.setCellFactory(lv -> new ListCell<>() {
            /**
             * @param item the result this cell now represents, or null if empty
             * @param empty whether this cell no longer represents any result
             */
            @Override
            protected void updateItem(LocationSearchResult item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                getStyleClass().add("transparent-cell");
                setGraphic(empty || item == null ? null
                        : new ResultCard(item, () -> onViewDetails.accept(item), isCustomer, onFavouriteAuthRequired,
                                favouriteIds.contains(item.location().getId()), nowFavourite -> {
                                    if (nowFavourite) {
                                        favouriteIds.add(item.location().getId());
                                    } else {
                                        favouriteIds.remove(item.location().getId());
                                    }
                                    onFavouriteToggle.accept(item, nowFavourite);
                                }));
                setPadding(new Insets(6, 10, 6, 10));
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                onSelected.accept(newItem);
            }
        });

        getStyleClass().add(Styles.BG_SUBTLE);
        VBox.setVgrow(listView, Priority.ALWAYS);
        getChildren().addAll(header, listView);
    }

    /**
     * Function to replace the set of favourited location ids, refreshing every visible card so
     * its heart reflects the new state. Called by the host screen once per search after loading
     * the current customer's favourites.
     *
     * @param ids the ids of the locations the current customer has favourited
     */
    public void setFavouriteIds(Set<String> ids) {
        favouriteIds.clear();
        favouriteIds.addAll(ids);
        listView.refresh();
    }

    /**
     * Function to re-sort and re-count the results after the underlying list changes (e.g. a new
     * search completed).
     */
    public void refresh() {
        resort();
        countLabel.setText(results.size() + " ristoranti trovati");
    }

    /**
     * Function to sort the shared results list in place by the currently picked
     * {@link ResultsSortOption}, a no-op if that option has no comparator (e.g. "relevance").
     */
    private void resort() {
        ResultsSortOption option = ResultsSortOption.fromLabel(sortBox.getValue());
        if (option.comparator() != null) {
            results.sort(option.comparator());
        }
    }
}
