package com.strazzullo_marocco_sibilla_marin.app.remote;

import marocco.Booking;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Remote service interface exposing booking management via RMI.
 * Bookings target fixed 30-minute time slots starting from a location's opening time.
 * Creation checks the location's seat capacity for the requested slot: if full, the booking
 * is queued with {@code waiting} status instead of being rejected, and promoted automatically
 * once a seat frees up.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface BookingService extends Remote {

    /**
     * Function to list the fixed 30-minute time slots available at a location on a given date,
     * computed from the location's opening hours for that day of week.
     *
     * @param locationId the location id
     * @param date the date to compute slots for
     * @return the list of time slots, in chronological order; empty if the location is closed that day
     * @throws RemoteException if the location does not exist or a remote communication error occurs
     */
    List<LocalTime> getAvailableTimeSlots(String locationId, LocalDate date) throws RemoteException;

    /**
     * Function to compute how many seats are still free at a location, for a given date and time slot.
     *
     * @param locationId the location id
     * @param date the booking date
     * @param timeSlot the time slot
     * @return the number of free seats, never negative
     * @throws RemoteException if the location does not exist or a remote communication error occurs
     */
    int getAvailableSeats(String locationId, LocalDate date, LocalTime timeSlot) throws RemoteException;

    /**
     * Function to create a new booking for a user. If the requested slot has enough free seats,
     * the booking is created with {@code confirmed} status; otherwise it is queued with
     * {@code waiting} status and a waiting list position.
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
    Booking createBooking(String userId, String locationId, LocalDate date, LocalTime timeSlot, int seats) throws RemoteException;

    /**
     * Function to cancel a booking, after verifying that the requesting user owns it.
     * If the cancelled booking was {@code confirmed}, the earliest waiting bookings for the
     * same slot are promoted to {@code confirmed} as seats free up.
     *
     * @param userId the id of the user performing the cancellation
     * @param bookingId the id of the booking to cancel
     * @throws RemoteException if the user does not own the booking, the booking does not exist,
     *                          or a remote communication error occurs
     */
    void cancelBooking(String userId, String bookingId) throws RemoteException;

    /**
     * Function to list every booking made by a user.
     *
     * @param userId the user id
     * @return the list of bookings made by the user
     * @throws RemoteException if a remote communication error occurs
     */
    List<Booking> listBookingsByUser(String userId) throws RemoteException;

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
    List<Booking> listBookingsByLocation(String managerId, String locationId, LocalDate date) throws RemoteException;
}
