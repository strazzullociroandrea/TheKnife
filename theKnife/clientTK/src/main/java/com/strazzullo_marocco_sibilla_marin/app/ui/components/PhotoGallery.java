package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import sibilla.Photo;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Horizontal, scrollable strip of a location's photo gallery, loaded asynchronously via {@link
 * com.strazzullo_marocco_sibilla_marin.app.remote.PhotoService}. Hides itself entirely if the
 * location has no photos yet, rather than showing an empty strip.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class PhotoGallery extends ScrollPane {

    private static final Logger LOGGER = Logger.getLogger(PhotoGallery.class.getName());
    private static final double THUMBNAIL_SIZE = 220;

    private final HBox thumbnails = new HBox(12);

    /**
     * PhotoGallery constructor. Starts loading the location's photos immediately.
     *
     * @param locationId the location whose photo gallery to show
     */
    public PhotoGallery(String locationId) {
        setContent(thumbnails);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        getStyleClass().add("tk-photo-gallery");
        setVisible(false);
        setManaged(false);

        Task<List<Photo>> task = new Task<>() {
            @Override
            protected List<Photo> call() throws Exception {
                return ServiceLocator.getInstance().getPhotoService().listPhotos(locationId);
            }
        };
        task.setOnSucceeded(e -> showPhotos(task.getValue()));
        task.setOnFailed(e -> LOGGER.log(Level.WARNING, "[photos] failed to load gallery for location " + locationId,
                task.getException()));

        Thread thread = new Thread(task, "photo-gallery-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to render every photo as a thumbnail and reveal the gallery, left hidden if there
     * are none.
     *
     * @param photos the location's photos, possibly null or empty
     */
    private void showPhotos(List<Photo> photos) {
        if (photos == null || photos.isEmpty()) {
            return;
        }
        for (Photo photo : photos) {
            thumbnails.getChildren().add(thumbnail(photo));
        }
        setVisible(true);
        setManaged(true);
    }

    /**
     * Function to build a single fixed-size, rounded-corner photo thumbnail.
     *
     * @param photo the photo to render
     * @return the thumbnail
     */
    private StackPane thumbnail(Photo photo) {
        ImageView imageView = new ImageView(new Image(photo.getUrl(), THUMBNAIL_SIZE, THUMBNAIL_SIZE, false, true, true));
        imageView.setFitWidth(THUMBNAIL_SIZE);
        imageView.setFitHeight(THUMBNAIL_SIZE);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        clip.setArcWidth(16);
        clip.setArcHeight(16);

        StackPane frame = new StackPane(imageView);
        frame.setPadding(Insets.EMPTY);
        frame.getStyleClass().add("tk-photo-thumb");
        frame.setClip(clip);
        return frame;
    }
}
