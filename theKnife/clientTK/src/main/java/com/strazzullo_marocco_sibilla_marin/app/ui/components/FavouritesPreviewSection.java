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
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.LocationSearchResult;

import java.util.List;
import java.util.function.Consumer;

/**
 * The home screen's "I tuoi preferiti" preview row: a header ("I tuoi preferiti" + "Vedi tutti")
 * and up to {@value #PREVIEW_SIZE} of the customer's most recently favourited locations, hidden
 * entirely while there are none. Every card here is already a favourite, so unfavouriting one
 * removes it and reloads the row (backfilling from the next most recent favourite, if any) rather
 * than needing to track favourite ids from anywhere else on the screen.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class FavouritesPreviewSection extends VBox {

    private static final int PREVIEW_SIZE = 6;

    private final String userId;
    private final Consumer<LocationSearchResult> onViewDetails;
    private final HBox row = new HBox(16);

    /**
     * FavouritesPreviewSection constructor. Starts hidden and begins the background load
     * immediately.
     *
     * @param userId the id of the logged-in customer
     * @param onViewDetails callback invoked with a result when its card is clicked
     * @param onViewAll callback invoked when the "Vedi tutti" link is clicked
     */
    public FavouritesPreviewSection(String userId, Consumer<LocationSearchResult> onViewDetails, Runnable onViewAll) {
        this.userId = userId;
        this.onViewDetails = onViewDetails;

        Label title = new Label("I tuoi preferiti", new FontIcon(Feather.HEART));
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

        setSpacing(16);
        getChildren().addAll(header, row);
        setVisible(false);
        setManaged(false);

        load();
    }

    /**
     * Function to fetch up to {@value #PREVIEW_SIZE} favourites in the background and render
     * them, hiding the whole section when there are none.
     */
    private void load() {
        Task<List<LocationSearchResult>> task = new Task<>() {
            @Override
            protected List<LocationSearchResult> call() throws Exception {
                return ServiceLocator.getInstance().getFavouriteService().listFavourites(userId, 0, PREVIEW_SIZE);
            }
        };
        task.setOnSucceeded(e -> {
            List<LocationSearchResult> favourites = task.getValue();
            row.getChildren().clear();
            for (LocationSearchResult result : favourites) {
                row.getChildren().add(new RestaurantTileCard(result, false, true,
                        () -> onViewDetails.accept(result), nowFavourite -> removeFavourite(result)));
            }
            boolean empty = favourites.isEmpty();
            setVisible(!empty);
            setManaged(!empty);
        });
        Thread thread = new Thread(task, "favourites-preview-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to remove a location from the customer's favourites and reload the row.
     *
     * @param result the location to remove
     */
    private void removeFavourite(LocationSearchResult result) {
        String locationId = result.location().getId();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ServiceLocator.getInstance().getFavouriteService().removeFavourite(userId, locationId);
                return null;
            }
        };
        task.setOnSucceeded(e -> load());
        Thread thread = new Thread(task, "favourites-preview-remove-worker");
        thread.setDaemon(true);
        thread.start();
    }
}
