package com.strazzullo_marocco_sibilla_marin.app.rmi;

import com.strazzullo_marocco_sibilla_marin.app.remote.BookingService;
import com.strazzullo_marocco_sibilla_marin.app.remote.CustomerService;
import com.strazzullo_marocco_sibilla_marin.app.remote.LocationService;
import com.strazzullo_marocco_sibilla_marin.app.remote.PhotoService;
import com.strazzullo_marocco_sibilla_marin.app.remote.ReviewService;
import com.strazzullo_marocco_sibilla_marin.app.remote.AuthService;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Resolves and holds the RMI stubs the client UI talks to, looked up once from the registry
 * exposed by the TheKnife server.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public final class ServiceLocator {

    private static final String CUSTOMER_SERVICE_NAME = "CustomerService";
    private static final String LOCATION_SERVICE_NAME = "LocationService";
    private static final String BOOKING_SERVICE_NAME = "BookingService";
    private static final String PHOTO_SERVICE_NAME = "PhotoService";
    private static final String REVIEW_SERVICE_NAME = "ReviewService";
    private static final String AUTH_SERVICE_NAME = "AuthService";

    private static ServiceLocator instance;

    private final CustomerService customerService;
    private final LocationService locationService;
    private final BookingService bookingService;
    private final PhotoService photoService;
    private final ReviewService reviewService;
    private final AuthService authService;

    private ServiceLocator(CustomerService customerService, LocationService locationService,
                           BookingService bookingService, PhotoService photoService, ReviewService reviewService, AuthService authService) {
        this.customerService = customerService;
        this.locationService = locationService;
        this.bookingService = bookingService;
        this.photoService = photoService;
        this.reviewService = reviewService;
        this.authService = authService;
    }

    /**
     * Function to connect to the RMI registry and resolve every remote service stub.
     * Replaces any previously resolved instance.
     *
     * @param host the RMI registry host
     * @param port the RMI registry port
     * @return the connected service locator
     * @throws RemoteException   if the registry cannot be reached
     * @throws NotBoundException if a service is not bound on the registry
     */
    public static synchronized ServiceLocator connect(String host, int port) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(host, port);
        CustomerService customerService = (CustomerService) registry.lookup(CUSTOMER_SERVICE_NAME);
        LocationService locationService = (LocationService) registry.lookup(LOCATION_SERVICE_NAME);
        BookingService bookingService = (BookingService) registry.lookup(BOOKING_SERVICE_NAME);
        PhotoService photoService = (PhotoService) registry.lookup(PHOTO_SERVICE_NAME);
        ReviewService reviewService = (ReviewService) registry.lookup(REVIEW_SERVICE_NAME);
        AuthService authService = (AuthService) registry.lookup(AUTH_SERVICE_NAME);

        instance = new ServiceLocator(customerService, locationService, bookingService, photoService, reviewService, authService);
        return instance;
    }

    /**
     * Function to retrieve the already-connected service locator.
     *
     * @return the connected service locator
     * @throws IllegalStateException if {@link #connect(String, int)} was never called successfully
     */
    public static ServiceLocator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ServiceLocator is not connected yet. Call connect(host, port) first.");
        }
        return instance;
    }

    /**
     * Returns the auth service stub
     *
     * @return the auth service stub
     */
    public AuthService getAuthService() {
        return authService;
    }

    /**
     * Returns the customer service stub
     *
     * @return the customer service stub
     */
    public CustomerService getCustomerService() {
        return customerService;
    }

    /**
     * Returns the location service stub
     *
     * @return the location service stub
     */
    public LocationService getLocationService() {
        return locationService;
    }

    /**
     * Returns the booking service stub
     *
     * @return the booking service stub
     */
    public BookingService getBookingService() {
        return bookingService;
    }

    /**
     * Returns the photo service stub
     *
     * @return the photo service stub
     */
    public PhotoService getPhotoService() {
        return photoService;
    }

    /**
     * Returns the review service stub
     *
     * @return the review service stub
     */
    public ReviewService getReviewService() {
        return reviewService;
    }
}
