package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.ui.AppShell;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.LocationSearchResult;

import java.util.List;

/**
 * A custom component representing a paginated section of location cards on the Home view.
 * Handles display title, pagination state, and updates list items dynamically.
 *
 * @version 1.0
 */
public class HomeSectionPanel extends VBox {

    /**
     * HomeSectionPanel constructor.
     *
     * @param shell the AppShell for navigation
     * @param title the title of the section
     * @param items the list of locations to display
     * @param emptyMessage message shown when the list of items is empty
     * @param onFavouriteAuthRequired callback invoked when a non-customer clicks the heart button
     * @param onFavoriteToggled callback invoked when a customer toggles the favorite status
     */
    public HomeSectionPanel(AppShell shell, String title, List<LocationSearchResult> items, String emptyMessage,
                            Runnable onFavouriteAuthRequired, Runnable onFavoriteToggled) {
        super(12);

        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TITLE_2);
        titleLabel.setStyle("-fx-font-weight: bold;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        headerRow.getChildren().add(titleLabel);

        HBox cardRow = new HBox(16);
        cardRow.setAlignment(Pos.CENTER_LEFT);

        if (items == null || items.isEmpty()) {
            Label emptyLabel = new Label(emptyMessage);
            emptyLabel.getStyleClass().add(Styles.TEXT_MUTED);
            cardRow.getChildren().add(emptyLabel);
            getChildren().addAll(headerRow, cardRow);
            return;
        }

        int pageSize = 4;
        int totalItems = items.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        final int[] currentPage = {0};

        Label pageLabel = new Label(String.format("Pagina %d di %d", currentPage[0] + 1, totalPages));
        pageLabel.getStyleClass().add(Styles.TEXT_MUTED);

        Button prevButton = new Button("", new FontIcon(Feather.CHEVRON_LEFT));
        prevButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        prevButton.setDisable(true);

        Button nextButton = new Button("", new FontIcon(Feather.CHEVRON_RIGHT));
        nextButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        if (totalPages <= 1) {
            nextButton.setDisable(true);
        }

        Runnable updateCards = () -> {
            cardRow.getChildren().clear();
            int start = currentPage[0] * pageSize;
            int end = Math.min(start + pageSize, totalItems);
            for (int i = start; i < end; i++) {
                LocationSearchResult item = items.get(i);
                VerticalLocationCard card = new VerticalLocationCard(shell, item, onFavouriteAuthRequired, onFavoriteToggled);
                cardRow.getChildren().add(card);
            }
            pageLabel.setText(String.format("Pagina %d di %d", currentPage[0] + 1, totalPages));
            prevButton.setDisable(currentPage[0] == 0);
            nextButton.setDisable(currentPage[0] == totalPages - 1);
        };

        prevButton.setOnAction(e -> {
            if (currentPage[0] > 0) {
                currentPage[0]--;
                updateCards.run();
            }
        });

        nextButton.setOnAction(e -> {
            if (currentPage[0] < totalPages - 1) {
                currentPage[0]++;
                updateCards.run();
            }
        });

        HBox paginationControls = new HBox(6, pageLabel, prevButton, nextButton);
        paginationControls.setAlignment(Pos.CENTER_LEFT);
        headerRow.getChildren().add(paginationControls);

        updateCards.run();

        getChildren().addAll(headerRow, cardRow);
    }
}
