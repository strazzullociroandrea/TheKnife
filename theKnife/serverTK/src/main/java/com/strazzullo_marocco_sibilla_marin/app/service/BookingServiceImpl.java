package com.strazzullo_marocco_sibilla_marin.app.service;

import com.strazzullo_marocco_sibilla_marin.app.dao.BookingDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.LocationDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.RestaurantDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.BookingDAOImpl;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.LocationDAOImpl;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.RestaurantDAOImpl;
import com.strazzullo_marocco_sibilla_marin.app.remote.BookingService;
import marocco.Booking;
import sibilla.Day;
import sibilla.Location;
import sibilla.OpeningHours;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation of {@link BookingService} RMI remote interface.
 * Exposes booking creation, cancellation, and lookup to RMI clients. Time slots are fixed to a
 * 30-minute grid starting from the location's opening time for the requested day of week; seat
 * capacity is enforced against the location's {@code max_capacity}, with bookings that exceed it
 * queued as {@code waiting} instead of rejected. Decouples logic from database access via
 * {@link BookingDAO} and {@link LocationDAO}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class BookingServiceImpl extends UnicastRemoteObject implements BookingService {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(BookingServiceImpl.class.getName());

    /** Fixed length, in minutes, of every booking time slot. */
    private static final int SLOT_MINUTES = 30;

    private final BookingDAO bookingDAO;
    private final LocationDAO locationDAO;
    private final RestaurantDAO restaurantDAO;

    /**
     * BookingServiceImpl constructor with custom DAOs injected.
     *
     * @param bookingDAO the booking DAO implementation to use
     * @param locationDAO the location DAO implementation to use, for capacity and opening hours
     * @param restaurantDAO the restaurant DAO implementation to use, for ownership checks
     * @throws RemoteException if RMI export fails
     */
    public BookingServiceImpl(BookingDAO bookingDAO, LocationDAO locationDAO, RestaurantDAO restaurantDAO) throws RemoteException {
        super();
        this.bookingDAO = bookingDAO;
        this.locationDAO = locationDAO;
        this.restaurantDAO = restaurantDAO;
    }

    /**
     * BookingServiceImpl constructor. Exports the remote object and initializes the DAO layer.
     *
     * @throws RemoteException if RMI export fails
     */
    public BookingServiceImpl() throws RemoteException {
        this(new BookingDAOImpl(), new LocationDAOImpl(), new RestaurantDAOImpl());
    }

    /**
     * Function to list the fixed 30-minute time slots available at a location on a given date,
     * computed from the location's opening hours for that day of week.
     *
     * @param locationId the location id
     * @param date the date to compute slots for
     * @return the list of time slots, in chronological order; empty if the location is closed that day
     * @throws RemoteException if the location does not exist or a remote communication error occurs
     */
    @Override
    public List<LocalTime> getAvailableTimeSlots(String locationId, LocalDate date) throws RemoteException {
        try {
            Location location = requireLocation(locationId);
            return computeSlots(location, date);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in getAvailableTimeSlots service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to compute how many seats are still free at a location, for a given date and time slot.
     *
     * @param locationId the location id
     * @param date the booking date
     * @param timeSlot the time slot
     * @return the number of free seats, never negative
     * @throws RemoteException if the location does not exist or a remote communication error occurs
     */
    @Override
    public int getAvailableSeats(String locationId, LocalDate date, LocalTime timeSlot) throws RemoteException {
        try {
            Location location = requireLocation(locationId);
            int booked = bookingDAO.sumConfirmedSeats(locationId, date, timeSlot);
            return Math.max(0, location.getMaxCapacity() - booked);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in getAvailableSeats service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to create a new booking for a user, after validating the requested slot against
     * the location's opening hours and 30-minute grid. The DAO layer resolves the final status
     * ({@code confirmed} or {@code waiting}) against the location's seat capacity.
     *
     * @param userId the id of the user making the booking
     * @param locationId the id of the location to book
     * @param date the booking date
     * @param timeSlot the requested time slot, must align to the location's 30-minute grid
     * @param seats the number of seats requested, must be positive and not exceed the location's capacity
     * @return the created booking, with its resolved status and waiting position (if any)
     * @throws RemoteException if the location is closed on that date/slot, the parameters are invalid,
     *                          or a remote communication error occurs
     */
    @Override
    public Booking createBooking(String userId, String locationId, LocalDate date, LocalTime timeSlot, int seats) throws RemoteException {
        try {
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("userId is null or empty");
            }
            if (date == null || timeSlot == null) {
                throw new IllegalArgumentException("date or timeSlot is null");
            }
            if (seats <= 0) {
                throw new IllegalArgumentException("seats must be positive");
            }
            if (isInThePast(date, timeSlot)) {
                throw new IllegalArgumentException("Cannot book a date/time slot in the past: " + date + " " + timeSlot);
            }

            Location location = requireLocation(locationId);
            if (seats > location.getMaxCapacity()) {
                throw new IllegalArgumentException("Requested seats exceed location capacity: " + location.getMaxCapacity());
            }

            List<LocalTime> slots = computeSlots(location, date);
            if (!slots.contains(timeSlot)) {
                throw new IllegalArgumentException("Location " + locationId + " has no slot " + timeSlot + " on " + date);
            }

            Booking booking = new Booking(userId, locationId, date, timeSlot, seats);
            Booking created = bookingDAO.create(booking, location.getMaxCapacity());
            LOGGER.info(() -> "Created booking " + created.getId() + " for location " + locationId + " with status " + created.getStatus());
            return created;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in createBooking service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to cancel a booking, after verifying that the requesting user owns it.
     *
     * @param userId the id of the user performing the cancellation
     * @param bookingId the id of the booking to cancel
     * @throws RemoteException if the user does not own the booking, the booking does not exist,
     *                          or a remote communication error occurs
     */
    @Override
    public void cancelBooking(String userId, String bookingId) throws RemoteException {
        try {
            Booking booking = bookingDAO.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
            if (!booking.getUserId().equals(userId)) {
                throw new IllegalArgumentException("User " + userId + " does not own booking " + bookingId);
            }

            Location location = requireLocation(booking.getLocationId());
            bookingDAO.cancel(bookingId, location.getMaxCapacity());
            LOGGER.info(() -> "Cancelled booking " + bookingId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in cancelBooking service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to list every booking made by a user.
     *
     * @param userId the user id
     * @return the list of bookings made by the user
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public List<Booking> listBookingsByUser(String userId) throws RemoteException {
        try {
            return bookingDAO.findByUser(userId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in listBookingsByUser service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        }
    }

    /**
     * Function to list every booking at a location on a given date, after verifying that the
     * requesting manager owns the restaurant the location belongs to.
     *
     * @param managerId the id of the manager performing the request
     * @param locationId the location id
     * @param date the date to list bookings for
     * @return the list of bookings at the location on that date
     * @throws RemoteException if the manager does not own the location, the location does not exist,
     *                          or a remote communication error occurs
     */
    @Override
    public List<Booking> listBookingsByLocation(String managerId, String locationId, LocalDate date) throws RemoteException {
        try {
            requireOwnedLocation(managerId, locationId);
            return bookingDAO.findByLocation(locationId, date);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException encountered in listBookingsByLocation service method", e);
            throw new RemoteException("Database access exception occurred on the server side.", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Function to retrieve a location, failing fast if it does not exist.
     *
     * @param locationId the location id
     * @return the location
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the location does not exist
     */
    private Location requireLocation(String locationId) throws SQLException {
        return locationDAO.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));
    }

    /**
     * Function to verify that a manager owns the restaurant a location belongs to.
     *
     * @param managerId the id of the manager
     * @param locationId the id of the location
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the location does not exist or the manager does not own it
     */
    private void requireOwnedLocation(String managerId, String locationId) throws SQLException {
        String restaurantId = locationDAO.findRestaurantIdById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));
        if (!Boolean.TRUE.equals(restaurantDAO.isOwner(restaurantId, managerId))) {
            throw new IllegalArgumentException("Manager " + managerId + " does not own location " + locationId);
        }
    }

    /**
     * Function to compute the fixed 30-minute time slots for a location on a given date, from its
     * opening hours for that day of week. Returns an empty list if the location has no opening
     * hours entry for that day, or if the entry is malformed.
     *
     * @param location the location to compute slots for
     * @param date the date to compute slots for
     * @return the list of time slots, in chronological order
     */
    private List<LocalTime> computeSlots(Location location, LocalDate date) {
        List<LocalTime> slots = new ArrayList<>();
        if (date == null) {
            return slots;
        }

        Day day = Day.valueOf(date.getDayOfWeek().name().toLowerCase());
        String hours = location.getOpeningTimes() != null ? location.getOpeningTimes().get(day) : null;
        LocalTime[] range = OpeningHours.parseRange(hours);
        if (range == null) {
            return slots;
        }

        LocalTime cursor = range[0];
        while (cursor.isBefore(range[1])) {
            if (!isInThePast(date, cursor)) {
                slots.add(cursor);
            }
            cursor = cursor.plusMinutes(SLOT_MINUTES);
        }
        return slots;
    }

    /**
     * Function to check whether a date/time slot has already passed, so today's already-elapsed
     * slots are excluded from {@link #computeSlots(Location, LocalDate)} and rejected by {@link
     * #createBooking(String, String, LocalDate, LocalTime, int)} rather than silently accepted.
     *
     * @param date the slot's date
     * @param timeSlot the slot's time
     * @return true if the date/time is strictly before the current moment
     */
    private boolean isInThePast(LocalDate date, LocalTime timeSlot) {
        return LocalDateTime.of(date, timeSlot).isBefore(LocalDateTime.now());
    }
}
