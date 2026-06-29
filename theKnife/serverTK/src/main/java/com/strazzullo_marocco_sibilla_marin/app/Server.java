package com.strazzullo_marocco_sibilla_marin.app;

import com.strazzullo_marocco_sibilla_marin.app.service.BookingServiceImpl;
import com.strazzullo_marocco_sibilla_marin.app.service.CustomerServiceImpl;
import com.strazzullo_marocco_sibilla_marin.app.service.AuthServiceImpl;
import com.strazzullo_marocco_sibilla_marin.app.service.LocationServiceImpl;
import com.strazzullo_marocco_sibilla_marin.app.service.PhotoServiceImpl;
import com.strazzullo_marocco_sibilla_marin.app.service.RestaurantServiceImpl;
import com.strazzullo_marocco_sibilla_marin.app.service.ReviewServiceImpl;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.ReviewDAOImpl;
import com.strazzullo_marocco_sibilla_marin.app.config.DotEnv;
import com.strazzullo_marocco_sibilla_marin.app.config.EnvSetup;
import com.strazzullo_marocco_sibilla_marin.app.config.PhotoStorageConfig;
import com.strazzullo_marocco_sibilla_marin.app.storage.S3PhotoStorage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for theKnife RMI server. Connects to the database and binds every
 * remote service onto the RMI registry. No explicit blocking is needed after that:
 * the registry's own non-daemon threads keep the JVM alive once main() returns.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA - author of this file
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    public static final int RMI_PORT = 1099;
    public static final String CUSTOMER_SERVICE_NAME = "CustomerService";
    public static final String AUTH_SERVICE_NAME = "AuthService";
    public static final String LOCATION_SERVICE_NAME = "LocationService";
    public static final String RESTAURANT_SERVICE_NAME = "RestaurantService";
    public static final String BOOKING_SERVICE_NAME = "BookingService";
    public static final String PHOTO_SERVICE_NAME = "PhotoService";
    public static final String REVIEW_SERVICE_NAME = "ReviewService";

    /**
     * Starts the server: runs the interactive setup wizard if any required environment variables
     * are missing, connects to the database, creates the RMI registry, and binds all remote
     * services. The JVM stays alive after this method returns because the registry's internal
     * threads are non-daemon.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        EnvSetup.run();
        LOGGER.info("Avvio del progetto in corso...");

        try {
            String url = DotEnv.get("DATABASE_URL");
            String username = DotEnv.get("USER_DB");
            String password = DotEnv.get("PASS_DB");
            DBConnectionPool.getInstance(url, username, password);

            Registry registry = LocateRegistry.createRegistry(RMI_PORT);

            registry.rebind(CUSTOMER_SERVICE_NAME, new CustomerServiceImpl());
            LOGGER.info(() -> "CustomerService bound on RMI registry, port " + RMI_PORT);

            registry.rebind(AUTH_SERVICE_NAME, new AuthServiceImpl());
            LOGGER.info(() -> "AuthService bound on RMI registry, port " + RMI_PORT);

            registry.rebind(LOCATION_SERVICE_NAME, new LocationServiceImpl());
            LOGGER.info(() -> "LocationService bound on RMI registry, port " + RMI_PORT);

            registry.rebind(RESTAURANT_SERVICE_NAME, new RestaurantServiceImpl());
            LOGGER.info(() -> "RestaurantService bound on RMI registry, port " + RMI_PORT);

            registry.rebind(BOOKING_SERVICE_NAME, new BookingServiceImpl());
            LOGGER.info(() -> "BookingService bound on RMI registry, port " + RMI_PORT);

            registry.rebind(REVIEW_SERVICE_NAME, new ReviewServiceImpl(new ReviewDAOImpl()));
            LOGGER.info(() -> "ReviewService bound on RMI registry, port " + RMI_PORT);

            bindPhotoService(registry);

            LOGGER.info("In attesa di richieste RMI...");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Avvio del server fallito", e);
        }
    }

    /**
     * Function to bind {@link PhotoServiceImpl}, kept separate from the rest of {@link
     * #main(String[])} so that a missing/invalid {@code PHOTO_S3_*} configuration only disables
     * the photo gallery rather than aborting the entire server startup (every other service is
     * already bound and serving requests by the time this runs).
     *
     * @param registry the RMI registry to bind onto
     */
    private static void bindPhotoService(Registry registry) {
        try {
            registry.rebind(PHOTO_SERVICE_NAME, new PhotoServiceImpl(new S3PhotoStorage(new PhotoStorageConfig())));
            LOGGER.info(() -> "PhotoService bound on RMI registry, port " + RMI_PORT);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "PhotoService not bound (check PHOTO_S3_* configuration); the rest of the server is unaffected", e);
        }
    }
}
