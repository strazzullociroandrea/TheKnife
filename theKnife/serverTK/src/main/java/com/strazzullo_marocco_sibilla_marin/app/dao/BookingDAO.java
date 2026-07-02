package com.strazzullo_marocco_sibilla_marin.app.dao;

import marocco.Booking;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for booking entity operations.
 * Resolves seat availability and the waiting list atomically with respect to concurrent
 * requests for the same location/date/time slot.
 * Follows the Dependency Inversion Principle, decoupling business logic from JDBC implementation.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface BookingDAO {

    /**
     * Retrieves a booking by its unique identifier.
     *
     * @param id the unique identifier of the booking
     * @return an Optional containing the booking if found, or empty if not found
     * @throws SQLException if a database query error occurs
     */
    Optional<Booking> findById(String id) throws SQLException;

    /**
     * Retrieves every booking made by a specific user.
     *
     * @param userId the id of the user
     * @return a list of bookings made by the user
     * @throws SQLException if a database query error occurs
     */
    List<Booking> findByUser(String userId) throws SQLException;

    /**
     * Retrieves every booking at a location on a given date.
     *
     * @param locationId the location id
     * @param date the booking date
     * @return a list of bookings at the location on that date
     * @throws SQLException if a database query error occurs
     */
    List<Booking> findByLocation(String locationId, LocalDate date) throws SQLException;

    /**
     * Sums the seats of every {@code confirmed} booking at a location, for a given date and time slot.
     *
     * @param locationId the location id
     * @param date the booking date
     * @param timeSlot the time slot
     * @return the total number of confirmed seats for that slot
     * @throws SQLException if a database query error occurs
     */
    int sumConfirmedSeats(String locationId, LocalDate date, LocalTime timeSlot) throws SQLException;

    /**
     * Creates a new booking, resolving its status against the location's seat capacity for the
     * requested slot within the same transaction: if enough seats are free, the booking is stored
     * as {@code confirmed}; otherwise it is stored as {@code waiting} with the next free waiting
     * list position. Serializes concurrent requests for the same location/date/time slot with a
     * Postgres advisory lock so the capacity check and insert happen atomically.
     *
     * @param booking the booking to create; its status and waiting position are overwritten
     * @param maxCapacity the location's maximum seating capacity
     * @return the created booking, with its resolved status and waiting position
     * @throws SQLException if a database operation error occurs, or if the booking is null
     */
    Booking create(Booking booking, int maxCapacity) throws SQLException;

    /**
     * Cancels a booking. If the cancelled booking was {@code confirmed}, promotes the earliest
     * {@code waiting} bookings for the same location/date/time slot to {@code confirmed}, in
     * waiting list order, until the freed seats run out. Serializes concurrent requests for the
     * same location/date/time slot with a Postgres advisory lock so the promotion is atomic with
     * respect to other bookings or cancellations on that slot.
     *
     * @param bookingId the id of the booking to cancel
     * @param maxCapacity the location's maximum seating capacity
     * @throws SQLException if a database operation error occurs, or if the booking does not exist
     */
    void cancel(String bookingId, int maxCapacity) throws SQLException;
}
