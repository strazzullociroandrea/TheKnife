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

import java.util.function.Consumer;

/**
 * The search screen's results column: a "n ristoranti trovati" count plus a sort picker, and the
 * scrollable list of {@link ResultCard}s itself. Owns sorting the shared results list in place,
 * so {@link com.strazzullo_marocco_sibilla_marin.app.ui.SearchView} only has to call {@link
 * #refresh()} after a search completes.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public class SearchResultsPanel extends VBox {

    private final Label countLabel = new Label();
    private final ComboBox<String> sortBox = new ComboBox<>();
    private final ListView<LocationSearchResult> listView = new ListView<>();
    private final ObservableList<LocationSearchResult> results;

    /**
     * @param results the shared, mutable results list to display and sort
     * @param onSelected called with the newly selected result, e.g. to focus its map pin
     * @param onViewDetails called with a result when its card is clicked
     */
    public SearchResultsPanel(ObservableList<LocationSearchResult> results,
                               Consumer<LocationSearchResult> onSelected, Consumer<LocationSearchResult> onViewDetails) {
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
            @Override
            protected void updateItem(LocationSearchResult item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                getStyleClass().add("transparent-cell");
                setGraphic(empty || item == null ? null : new ResultCard(item, () -> onViewDetails.accept(item)));
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
     * Function to re-sort and re-count the results after the underlying list changes (e.g. a new
     * search completed).
     */
    public void refresh() {
        resort();
        countLabel.setText(results.size() + " ristoranti trovati");
    }

    private void resort() {
        ResultsSortOption option = ResultsSortOption.fromLabel(sortBox.getValue());
        if (option.comparator() != null) {
            results.sort(option.comparator());
        }
    }
}
