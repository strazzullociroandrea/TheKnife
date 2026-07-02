package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.Photo;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fixed-size square thumbnail showing a location's first gallery photo, loaded asynchronously via
 * {@link com.strazzullo_marocco_sibilla_marin.app.remote.PhotoService}. Falls back to the same
 * cuisine icon used by the quick-filter chips on an accent-tinted backdrop while loading and for
 * every location with no uploaded photos yet, so no card on the search results is ever a blank
 * tile. Used by {@link ResultCard}.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class LocationThumbnail extends StackPane {

    private static final Logger LOGGER = Logger.getLogger(LocationThumbnail.class.getName());
    private static final double SIZE = 88;

    private final double width;
    private final double height;

    /**
     * @param locationId the location whose first photo to show
     * @param cuisine the restaurant's cuisine type, used for the placeholder icon
     */
    public LocationThumbnail(String locationId, String cuisine) {
        this(locationId, cuisine, SIZE, SIZE);
    }

    /**
     * @param locationId the location whose first photo to show
     * @param cuisine the restaurant's cuisine type, used for the placeholder icon
     * @param width the thumbnail's width, in pixels
     * @param height the thumbnail's height, in pixels
     */
    public LocationThumbnail(String locationId, String cuisine, double width, double height) {
        this.width = width;
        this.height = height;

        getStyleClass().addAll("tk-thumbnail", "tk-thumbnail-placeholder");
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        setClip(clip);

        getChildren().add(placeholderIcon(cuisine));

        Task<List<Photo>> task = new Task<>() {
            @Override
            protected List<Photo> call() throws Exception {
                return ServiceLocator.getInstance().getPhotoService().listPhotos(locationId);
            }
        };
        task.setOnSucceeded(e -> {
            List<Photo> photos = task.getValue();
            if (photos != null && !photos.isEmpty()) {
                showPhoto(photos.get(0));
            }
        });
        task.setOnFailed(e -> LOGGER.log(Level.WARNING,
                "[thumbnail] failed to load photo for location " + locationId, task.getException()));

        Thread thread = new Thread(task, "location-thumbnail-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to build the placeholder shown while loading and for locations with no photos:
     * the cuisine's icon if one is bundled, or a generic image icon otherwise.
     *
     * @param cuisine the restaurant's cuisine type, or null
     * @return the placeholder node
     */
    private Node placeholderIcon(String cuisine) {
        var stream = cuisine == null ? null
                : getClass().getResourceAsStream("/icons/cuisine/" + cuisine.toLowerCase(Locale.ROOT) + ".png");
        if (stream == null) {
            return new FontIcon(Feather.IMAGE);
        }
        ImageView icon = new ImageView(new Image(stream));
        icon.setFitWidth(40);
        icon.setFitHeight(40);
        icon.setPreserveRatio(true);
        icon.setSmooth(true);
        return icon;
    }

    /**
     * Function to replace the placeholder with the location's first gallery photo.
     *
     * @param photo the photo to show
     */
    private void showPhoto(Photo photo) {
        getStyleClass().remove("tk-thumbnail-placeholder");
        ImageView imageView = new ImageView(new Image(photo.getUrl(), width, height, false, true, true));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        getChildren().setAll(imageView);
    }
}
