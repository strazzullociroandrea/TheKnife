/**
 *
 * Represents a photo belonging to a restaurant location's public gallery.
 *
 * Photos are stored in an S3-compatible object store (Garage) and only the
 * resulting public URL is kept in the relational database.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Marin Marco, 760622, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @version 1.0
 */

package sibilla;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Photo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the photo */
    private String id;
    /** Identifier of the location the photo belongs to */
    private String locationId;
    /** Public URL of the photo on the object storage */
    private String url;
    /** Date and time the photo was uploaded */
    private LocalDateTime uploadedAt;

    /**
     * An empty constructor for a photo. Nothing is passed to it.
     */
    public Photo() {
        id = UUID.randomUUID().toString();
        locationId = "";
        url = "";
        uploadedAt = LocalDateTime.now();
    }

    /**
     * Declares a photo with all parameters.
     *
     * @param id            photo's id
     * @param locationId    id of the location the photo belongs to
     * @param url            public url of the photo
     * @param uploadedAt    upload timestamp
     */
    public Photo(String id, String locationId, String url, LocalDateTime uploadedAt) {
        this.id = id;
        this.locationId = locationId;
        this.url = url;
        this.uploadedAt = uploadedAt;
    }

    /**
     * Declares a new photo not yet persisted, generating a random id and timestamp.
     *
     * @param locationId    id of the location the photo belongs to
     * @param url            public url of the photo
     */
    public Photo(String locationId, String url) {
        this.id = UUID.randomUUID().toString();
        this.locationId = locationId;
        this.url = url;
        this.uploadedAt = LocalDateTime.now();
    }

    /**
     * getId
     *
     * @return returns id
     */
    public String getId() {
        return id;
    }

    /**
     * setId
     *
     * @param id sets id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * getLocationId
     *
     * @return returns locationId
     */
    public String getLocationId() {
        return locationId;
    }

    /**
     * setLocationId
     *
     * @param locationId sets locationId
     */
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    /**
     * getUrl
     *
     * @return returns url
     */
    public String getUrl() {
        return url;
    }

    /**
     * setUrl
     *
     * @param url sets url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * getUploadedAt
     *
     * @return returns uploadedAt
     */
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    /**
     * setUploadedAt
     *
     * @param uploadedAt sets uploadedAt
     */
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id='" + id + '\'' +
                ", locationId='" + locationId + '\'' +
                ", url='" + url + '\'' +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
