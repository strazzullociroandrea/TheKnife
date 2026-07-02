package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * The date and time slot picked in {@link BookingPanel}, handed off to {@link BookingDialog}
 * which asks for the number of people and then calls {@code BookingService.createBooking}.
 *
 * @param locationId the location to book
 * @param date the picked date
 * @param timeSlot the picked time slot
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public record SlotSelection(String locationId, LocalDate date, LocalTime timeSlot) {
}
