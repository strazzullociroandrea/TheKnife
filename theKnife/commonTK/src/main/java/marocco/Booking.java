package marocco;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Represents a reservation made by a customer for a restaurant location in the TheKnife system.
 *
 * A booking targets a fixed 30-minute time slot, on a given date, at a given location.
 * Its status reflects whether seats were available at creation time ({@code confirmed}),
 * whether it is queued because the slot was full ({@code waiting}), or whether it was
 * cancelled or has expired.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 * @version 1.0
 */
public class Booking implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Identifier for the booking. Always randomly generated. */
    private String id;
    /** Identifier for the user who made the booking. */
    private String userId;
    /** Identifier for the location the booking is for. */
    private String locationId;
    /** Date the booking is for. */
    private LocalDate bookingDate;
    /** Fixed 30-minute time slot the booking is for. */
    private LocalTime timeSlot;
    /** Number of seats requested by the booking. */
    private int seats;
    /** Current status of the booking. */
    private BookingStatus status;
    /** Position in the waiting list for the slot, set only when status is {@code waiting}. */
    private Integer waitingPosition;

    /** Booking empty constructor. */
    public Booking() {
        id = UUID.randomUUID().toString();
        userId = "";
        locationId = "";
        bookingDate = null;
        timeSlot = null;
        seats = 0;
        status = BookingStatus.confirmed;
        waitingPosition = null;
    }

    /**
     * Booking constructor to create a booking request, before it has been resolved
     * to a status by the booking service.
     *
     * @param userId the id of the user making the booking
     * @param locationId the id of the location the booking is for
     * @param bookingDate the date the booking is for
     * @param timeSlot the time slot the booking is for
     * @param seats the number of seats requested
     */
    public Booking(String userId, String locationId, LocalDate bookingDate, LocalTime timeSlot, int seats) {
        id = UUID.randomUUID().toString();
        this.userId = userId;
        this.locationId = locationId;
        this.bookingDate = bookingDate;
        this.timeSlot = timeSlot;
        this.seats = seats;
        status = BookingStatus.confirmed;
        waitingPosition = null;
    }

    /**
     * Booking constructor with all attributes, used when reconstructing a booking already
     * persisted in the database.
     *
     * @param id the booking id
     * @param userId the id of the user who made the booking
     * @param locationId the id of the location the booking is for
     * @param bookingDate the date the booking is for
     * @param timeSlot the time slot the booking is for
     * @param seats the number of seats requested
     * @param status the booking status
     * @param waitingPosition the position in the waiting list, or null if not waiting
     */
    public Booking(String id, String userId, String locationId, LocalDate bookingDate, LocalTime timeSlot,
                    int seats, BookingStatus status, Integer waitingPosition) {
        this.id = id;
        this.userId = userId;
        this.locationId = locationId;
        this.bookingDate = bookingDate;
        this.timeSlot = timeSlot;
        this.seats = seats;
        this.status = status;
        this.waitingPosition = waitingPosition;
    }

    /**
     * Returns the booking id
     *
     * @return the booking id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the booking id
     *
     * @param id the booking id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the id of the user who made the booking
     *
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the id of the user who made the booking
     *
     * @param userId the user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the id of the location the booking is for
     *
     * @return the location id
     */
    public String getLocationId() {
        return locationId;
    }

    /**
     * Sets the id of the location the booking is for
     *
     * @param locationId the location id
     */
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    /**
     * Returns the date the booking is for
     *
     * @return the booking date
     */
    public LocalDate getBookingDate() {
        return bookingDate;
    }

    /**
     * Sets the date the booking is for
     *
     * @param bookingDate the booking date
     */
    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    /**
     * Returns the time slot the booking is for
     *
     * @return the time slot
     */
    public LocalTime getTimeSlot() {
        return timeSlot;
    }

    /**
     * Sets the time slot the booking is for
     *
     * @param timeSlot the time slot
     */
    public void setTimeSlot(LocalTime timeSlot) {
        this.timeSlot = timeSlot;
    }

    /**
     * Returns the number of seats requested by the booking
     *
     * @return the number of seats
     */
    public int getSeats() {
        return seats;
    }

    /**
     * Sets the number of seats requested by the booking
     *
     * @param seats the number of seats
     */
    public void setSeats(int seats) {
        this.seats = seats;
    }

    /**
     * Returns the booking status
     *
     * @return the booking status
     */
    public BookingStatus getStatus() {
        return status;
    }

    /**
     * Sets the booking status
     *
     * @param status the booking status
     */
    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    /**
     * Returns the position in the waiting list for the slot
     *
     * @return the waiting position, or null if the booking is not waiting
     */
    public Integer getWaitingPosition() {
        return waitingPosition;
    }

    /**
     * Sets the position in the waiting list for the slot
     *
     * @param waitingPosition the waiting position, or null if not waiting
     */
    public void setWaitingPosition(Integer waitingPosition) {
        this.waitingPosition = waitingPosition;
    }

    /**
     * Shows booking details.
     *
     * @return booking details
     */
    @Override
    public String toString() {
        return "Booking [id: " + id + "\tuserId: " + userId + "\tlocationId: " + locationId +
                "\nbookingDate: " + bookingDate + "\ttimeSlot: " + timeSlot + "\tseats: " + seats +
                "\nstatus: " + status + "\twaitingPosition: " + waitingPosition + "]";
    }
}
