package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import javafx.concurrent.Task;
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
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public class LocationThumbnail extends StackPane {

    private static final Logger LOGGER = Logger.getLogger(LocationThumbnail.class.getName());
    private static final double SIZE = 88;

    /**
     * @param locationId the location whose first photo to show
     * @param cuisine the restaurant's cuisine type, used for the placeholder icon
     */
    public LocationThumbnail(String locationId, String cuisine) {
        getStyleClass().addAll("tk-thumbnail", "tk-thumbnail-placeholder");
        setPrefSize(SIZE, SIZE);
        setMinSize(SIZE, SIZE);
        setMaxSize(SIZE, SIZE);

        Rectangle clip = new Rectangle(SIZE, SIZE);
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

    private javafx.scene.Node placeholderIcon(String cuisine) {
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

    private void showPhoto(Photo photo) {
        getStyleClass().remove("tk-thumbnail-placeholder");
        ImageView imageView = new ImageView(new Image(photo.getUrl(), SIZE, SIZE, false, true, true));
        imageView.setFitWidth(SIZE);
        imageView.setFitHeight(SIZE);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        getChildren().setAll(imageView);
    }
}
