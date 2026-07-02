package com.strazzullo_marocco_sibilla_marin.app.storage;

import com.strazzullo_marocco_sibilla_marin.app.config.PhotoStorageConfig;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;
import java.util.UUID;

/**
 * {@link PhotoStorage} implementation backed by an S3-compatible object
 * storage, namely a self-hosted Garage cluster. Uses the AWS SDK v2 S3 client
 * with path-style addressing, which Garage requires since it does not serve
 * virtual-hosted-style bucket subdomains by default.
 * Garage does not support per-object ACLs reliably, so public readability is
 * expected to be configured at the bucket level (Garage "website"/public
 * access on the bucket referenced by {@code PHOTO_S3_BUCKET}), not per-object.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class S3PhotoStorage implements PhotoStorage {

    private final PhotoStorageConfig config;
    private final S3Client client;

    /**
     * S3PhotoStorage constructor, building the underlying S3 client from configuration.
     *
     * @param config the photo storage configuration (endpoint, credentials, bucket, ...)
     */
    public S3PhotoStorage(PhotoStorageConfig config) {
        this.config = config;
        this.client = S3Client.builder()
                .endpointOverride(URI.create(config.getEndpoint()))
                .region(Region.of(config.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey())))
                .forcePathStyle(true)
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    /**
     * Function to upload a photo's raw bytes to the Garage bucket configured via
     * {@link PhotoStorageConfig#getBucket()}, namespacing the object key by location.
     *
     * @param locationId    id of the location the photo belongs to
     * @param fileName      original file name, used to infer the file extension and content type
     * @param content       raw photo bytes
     * @return the public URL the uploaded photo is reachable at
     * @throws PhotoStorageException if the upload fails
     */
    @Override
    public String upload(String locationId, String fileName, byte[] content) throws PhotoStorageException {
        String key = buildKey(locationId, fileName);
        try {
            client.putObject(
                    PutObjectRequest.builder()
                            .bucket(config.getBucket())
                            .key(key)
                            .contentType(guessContentType(fileName))
                            .contentLength((long) content.length)
                            .build(),
                    RequestBody.fromBytes(content));
        } catch (S3Exception e) {
            throw new PhotoStorageException("Failed to upload photo to object storage: " + e.getMessage(), e);
        }
        return config.getPublicBaseUrl() + "/" + key;
    }

    /**
     * Function to delete a previously uploaded photo from the Garage bucket,
     * deriving the object key from its public URL.
     *
     * @param url the public URL returned by {@link #upload}
     * @throws PhotoStorageException if the deletion fails
     */
    @Override
    public void delete(String url) throws PhotoStorageException {
        String key = keyFromUrl(url);
        try {
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            throw new PhotoStorageException("Failed to delete photo from object storage: " + e.getMessage(), e);
        }
    }

    /**
     * Function to recover an object's storage key from its public URL.
     *
     * @param url the photo's public URL
     * @return the object's storage key
     * @throws PhotoStorageException if the URL doesn't belong to the configured public base URL
     */
    private String keyFromUrl(String url) throws PhotoStorageException {
        String prefix = config.getPublicBaseUrl() + "/";
        if (!url.startsWith(prefix)) {
            throw new PhotoStorageException("Photo URL does not belong to the configured storage: " + url);
        }
        return url.substring(prefix.length());
    }

    /**
     * Function to build a fresh, collision-free storage key for a new upload, preserving the
     * original file's extension.
     *
     * @param locationId the location the photo belongs to
     * @param fileName the uploaded file's original name, used only for its extension
     * @return the new object's storage key
     */
    private String buildKey(String locationId, String fileName) {
        String extension = "";
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0) {
            extension = fileName.substring(dot).toLowerCase();
        }
        return "locations/" + locationId + "/" + UUID.randomUUID() + extension;
    }

    /**
     * Function to guess an upload's MIME type from its file extension.
     *
     * @param fileName the uploaded file's original name
     * @return the guessed content type, or {@code application/octet-stream} if unrecognized
     */
    private String guessContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}
