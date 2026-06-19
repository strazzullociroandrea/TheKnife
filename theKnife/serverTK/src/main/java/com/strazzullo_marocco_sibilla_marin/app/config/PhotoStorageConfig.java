package com.strazzullo_marocco_sibilla_marin.app.config;

/**
 * Configuration holder for the S3-compatible object storage (Garage, self-hosted)
 * used to store restaurant location photos. All values are read from environment
 * variables (falling back to a local ".env" file via {@link DotEnv}), so no
 * credential ever needs to be hardcoded.
 *
 * Required variables:
 * <ul>
 *     <li>PHOTO_S3_ENDPOINT - Garage S3 API endpoint, e.g. http://localhost:3900</li>
 *     <li>PHOTO_S3_REGION - region accepted by Garage, e.g. garage</li>
 *     <li>PHOTO_S3_BUCKET - bucket holding the public photo gallery</li>
 *     <li>PHOTO_S3_ACCESS_KEY - Garage access key id</li>
 *     <li>PHOTO_S3_SECRET_KEY - Garage secret access key</li>
 *     <li>PHOTO_S3_PUBLIC_BASE_URL - public base URL under which uploaded photos are served,
 *          e.g. the Garage web/public endpoint for the bucket</li>
 * </ul>
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class PhotoStorageConfig {

    private final String endpoint;
    private final String region;
    private final String bucket;
    private final String accessKey;
    private final String secretKey;
    private final String publicBaseUrl;

    /**
     * PhotoStorageConfig constructor, reading every required value from the environment.
     *
     * @throws IllegalStateException if any required variable is missing
     */
    public PhotoStorageConfig() {
        this.endpoint = require("PHOTO_S3_ENDPOINT");
        this.region = require("PHOTO_S3_REGION");
        this.bucket = require("PHOTO_S3_BUCKET");
        this.accessKey = require("PHOTO_S3_ACCESS_KEY");
        this.secretKey = require("PHOTO_S3_SECRET_KEY");
        this.publicBaseUrl = stripTrailingSlash(require("PHOTO_S3_PUBLIC_BASE_URL"));
    }

    private static String require(String key) {
        String value = DotEnv.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }

    private static String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    /**
     * getEndpoint
     *
     * @return returns the Garage S3 API endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * getRegion
     *
     * @return returns the configured region
     */
    public String getRegion() {
        return region;
    }

    /**
     * getBucket
     *
     * @return returns the bucket name
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * getAccessKey
     *
     * @return returns the access key id
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * getSecretKey
     *
     * @return returns the secret access key
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * getPublicBaseUrl
     *
     * @return returns the public base URL photos are served from, without trailing slash
     */
    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }
}
