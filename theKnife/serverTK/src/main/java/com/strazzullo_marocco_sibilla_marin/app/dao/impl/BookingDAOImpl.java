package com.strazzullo_marocco_sibilla_marin.app.dao.impl;

import com.strazzullo_marocco_sibilla_marin.app.DBConnectionPool;
import com.strazzullo_marocco_sibilla_marin.app.dao.BookingDAO;
import marocco.Booking;
import marocco.BookingStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Concrete JDBC implementation of the {@link BookingDAO} interface.
 * Serializes seat-capacity decisions for a given location/date/time slot with a Postgres
 * advisory transaction lock ({@code pg_advisory_xact_lock}), so concurrent booking requests
 * or cancellations on the same slot cannot race past the seat count check.
 * Connects to the PostgreSQL database via {@link DBConnectionPool}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class BookingDAOImpl implements BookingDAO {

    /**
     * Function to map a row of ResultSet to a {@link Booking} entity object.
     *
     * @param rs the ResultSet containing booking table rows
     * @return the constructed Booking entity
     * @throws SQLException if a database mapping error occurs
     */
    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        String id = rs.getString("booking_id");
        String userId = rs.getString("user_id");
        String locationId = rs.getString("location_id");
        LocalDate bookingDate = rs.getObject("booking_date", LocalDate.class);
        LocalTime timeSlot = rs.getObject("time_slot", LocalTime.class);
        int seats = rs.getInt("seats");
        BookingStatus status = BookingStatus.valueOf(rs.getString("status"));
        int waitingPositionValue = rs.getInt("waiting_position");
        Integer waitingPosition = rs.wasNull() ? null : waitingPositionValue;
        return new Booking(id, userId, locationId, bookingDate, timeSlot, seats, status, waitingPosition);
    }

    /**
     * Acquires a transaction-scoped advisory lock keyed on the location/date/time slot, so the
     * caller is the only one resolving capacity for that slot until it commits or rolls back.
     *
     * @param conn the connection whose transaction the lock is scoped to
     * @param locationId the location id
     * @param date the booking date
     * @param timeSlot the time slot
     * @throws SQLException if a database operation error occurs
     */
    private void lockSlot(Connection conn, String locationId, LocalDate date, LocalTime timeSlot) throws SQLException {
        String lockKey = locationId + "|" + date + "|" + timeSlot;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT pg_advisory_xact_lock(hashtext(?))")) {
            stmt.setString(1, lockKey);
            stmt.execute();
        }
    }

    /**
     * Retrieves a booking by its unique identifier. Borrows a connection from
     * {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param id the unique identifier of the booking
     * @return an Optional containing the booking if found, or empty if not found
     * @throws SQLException if a database query error occurs or the id is null/empty
     */
    @Override
    public Optional<Booking> findById(String id) throws SQLException {
        if (id == null || id.isEmpty()) {
            throw new SQLException("id is null or empty");
        }
        String query = "SELECT * FROM booking WHERE booking_id = ?";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToBooking(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Retrieves every booking made by a specific user. Borrows a connection from
     * {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param userId the id of the user
     * @return a list of bookings made by the user
     * @throws SQLException if a database query error occurs or the userId is null/empty
     */
    @Override
    public List<Booking> findByUser(String userId) throws SQLException {
        if (userId == null || userId.isEmpty()) {
            throw new SQLException("userId is null or empty");
        }
        String query = "SELECT * FROM booking WHERE user_id = ? ORDER BY booking_date, time_slot";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userId);

            List<Booking> bookings = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRowToBooking(rs));
                }
            }
            return bookings;
        }
    }

    /**
     * Retrieves every booking at a location on a given date. Borrows a connection from
     * {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param locationId the location id
     * @param date the booking date
     * @return a list of bookings at the location on that date
     * @throws SQLException if a database query error occurs or the parameters are invalid
     */
    @Override
    public List<Booking> findByLocation(String locationId, LocalDate date) throws SQLException {
        if (locationId == null || locationId.isEmpty()) {
            throw new SQLException("locationId is null or empty");
        }
        if (date == null) {
            throw new SQLException("date is null");
        }
        String query = "SELECT * FROM booking WHERE location_id = ? AND booking_date = ? ORDER BY time_slot";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, locationId);
            stmt.setObject(2, date);

            List<Booking> bookings = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRowToBooking(rs));
                }
            }
            return bookings;
        }
    }

    /**
     * Sums the seats of every {@code confirmed} booking at a location, for a given date and time slot.
     * Borrows a connection from {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param locationId the location id
     * @param date the booking date
     * @param timeSlot the time slot
     * @return the total number of confirmed seats for that slot
     * @throws SQLException if a database query error occurs or the parameters are invalid
     */
    @Override
    public int sumConfirmedSeats(String locationId, LocalDate date, LocalTime timeSlot) throws SQLException {
        if (locationId == null || locationId.isEmpty()) {
            throw new SQLException("locationId is null or empty");
        }
        if (date == null || timeSlot == null) {
            throw new SQLException("date or timeSlot is null");
        }
        String query = "SELECT COALESCE(SUM(seats), 0) AS booked FROM booking " +
                "WHERE location_id = ? AND booking_date = ? AND time_slot = ? AND status = 'confirmed'";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, locationId);
            stmt.setObject(2, date);
            stmt.setObject(3, timeSlot);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("booked");
            }
        }
    }

    /**
     * Creates a new booking, resolving its status against the location's seat capacity within
     * the same transaction, behind an advisory lock on the location/date/time slot. Borrows a
     * connection from {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param booking the booking to create; its status and waiting position are overwritten
     * @param maxCapacity the location's maximum seating capacity
     * @return the created booking, with its resolved status and waiting position
     * @throws SQLException if a database operation error occurs, or if the booking is null
     */
    @Override
    public Booking create(Booking booking, int maxCapacity) throws SQLException {
        if (booking == null) throw new SQLException("booking is null");
        if (booking.getId() == null || booking.getId().isEmpty()) {
            booking.setId(UUID.randomUUID().toString());
        }

        String sumQuery = "SELECT COALESCE(SUM(seats), 0) AS booked FROM booking " +
                "WHERE location_id = ? AND booking_date = ? AND time_slot = ? AND status = 'confirmed'";
        String maxWaitingQuery = "SELECT COALESCE(MAX(waiting_position), 0) AS max_pos FROM booking " +
                "WHERE location_id = ? AND booking_date = ? AND time_slot = ? AND status = 'waiting'";
        String insertQuery = "INSERT INTO booking(booking_id, user_id, location_id, booking_date, " +
                "time_slot, seats, status, waiting_position) VALUES (?,?,?,?,?,?,?::booking_status,?)";

        try (Connection conn = DBConnectionPool.getInstance().getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                lockSlot(conn, booking.getLocationId(), booking.getBookingDate(), booking.getTimeSlot());

                int booked;
                try (PreparedStatement stmt = conn.prepareStatement(sumQuery)) {
                    stmt.setString(1, booking.getLocationId());
                    stmt.setObject(2, booking.getBookingDate());
                    stmt.setObject(3, booking.getTimeSlot());
                    try (ResultSet rs = stmt.executeQuery()) {
                        rs.next();
                        booked = rs.getInt("booked");
                    }
                }

                if (booked + booking.getSeats() <= maxCapacity) {
                    booking.setStatus(BookingStatus.confirmed);
                    booking.setWaitingPosition(null);
                } else {
                    int maxPosition;
                    try (PreparedStatement stmt = conn.prepareStatement(maxWaitingQuery)) {
                        stmt.setString(1, booking.getLocationId());
                        stmt.setObject(2, booking.getBookingDate());
                        stmt.setObject(3, booking.getTimeSlot());
                        try (ResultSet rs = stmt.executeQuery()) {
                            rs.next();
                            maxPosition = rs.getInt("max_pos");
                        }
                    }
                    booking.setStatus(BookingStatus.waiting);
                    booking.setWaitingPosition(maxPosition + 1);
                }

                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setString(1, booking.getId());
                    stmt.setString(2, booking.getUserId());
                    stmt.setString(3, booking.getLocationId());
                    stmt.setObject(4, booking.getBookingDate());
                    stmt.setObject(5, booking.getTimeSlot());
                    stmt.setInt(6, booking.getSeats());
                    stmt.setString(7, booking.getStatus().name());
                    if (booking.getWaitingPosition() != null) {
                        stmt.setInt(8, booking.getWaitingPosition());
                    } else {
                        stmt.setNull(8, Types.INTEGER);
                    }
                    stmt.executeUpdate();
                }

                conn.commit();
                return booking;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    /**
     * Cancels a booking and, if it was {@code confirmed}, promotes the earliest waiting bookings
     * for the same slot as seats free up, behind an advisory lock on the location/date/time slot.
     * Borrows a connection from {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param bookingId the id of the booking to cancel
     * @param maxCapacity the location's maximum seating capacity
     * @throws SQLException if a database operation error occurs, or if the booking does not exist
     */
    @Override
    public void cancel(String bookingId, int maxCapacity) throws SQLException {
        if (bookingId == null || bookingId.isEmpty()) throw new SQLException("bookingId is null or empty");

        String findQuery = "SELECT location_id, booking_date, time_slot, status FROM booking WHERE booking_id = ?";
        String cancelQuery = "UPDATE booking SET status = 'cancelled', waiting_position = NULL WHERE booking_id = ?";
        String sumQuery = "SELECT COALESCE(SUM(seats), 0) AS booked FROM booking " +
                "WHERE location_id = ? AND booking_date = ? AND time_slot = ? AND status = 'confirmed'";
        String waitingQuery = "SELECT booking_id, seats FROM booking WHERE location_id = ? AND booking_date = ? " +
                "AND time_slot = ? AND status = 'waiting' ORDER BY waiting_position ASC";
        String promoteQuery = "UPDATE booking SET status = 'confirmed', waiting_position = NULL WHERE booking_id = ?";

        try (Connection conn = DBConnectionPool.getInstance().getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                String locationId;
                LocalDate date;
                LocalTime timeSlot;
                String status;
                try (PreparedStatement stmt = conn.prepareStatement(findQuery)) {
                    stmt.setString(1, bookingId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Booking not found: " + bookingId);
                        }
                        locationId = rs.getString("location_id");
                        date = rs.getObject("booking_date", LocalDate.class);
                        timeSlot = rs.getObject("time_slot", LocalTime.class);
                        status = rs.getString("status");
                    }
                }

                lockSlot(conn, locationId, date, timeSlot);

                try (PreparedStatement stmt = conn.prepareStatement(cancelQuery)) {
                    stmt.setString(1, bookingId);
                    stmt.executeUpdate();
                }

                if (BookingStatus.confirmed.name().equals(status)) {
                    int booked;
                    try (PreparedStatement stmt = conn.prepareStatement(sumQuery)) {
                        stmt.setString(1, locationId);
                        stmt.setObject(2, date);
                        stmt.setObject(3, timeSlot);
                        try (ResultSet rs = stmt.executeQuery()) {
                            rs.next();
                            booked = rs.getInt("booked");
                        }
                    }

                    int freeSeats = maxCapacity - booked;
                    if (freeSeats > 0) {
                        try (PreparedStatement waitingStmt = conn.prepareStatement(waitingQuery);
                             PreparedStatement promoteStmt = conn.prepareStatement(promoteQuery)) {
                            waitingStmt.setString(1, locationId);
                            waitingStmt.setObject(2, date);
                            waitingStmt.setObject(3, timeSlot);
                            try (ResultSet rs = waitingStmt.executeQuery()) {
                                while (freeSeats > 0 && rs.next()) {
                                    int waitingSeats = rs.getInt("seats");
                                    if (waitingSeats <= freeSeats) {
                                        promoteStmt.setString(1, rs.getString("booking_id"));
                                        promoteStmt.executeUpdate();
                                        freeSeats -= waitingSeats;
                                    }
                                }
                            }
                        }
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }
}
