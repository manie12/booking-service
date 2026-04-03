package io.booking.booking_service.dto.pojo.booking;

import io.booking.booking_service.datatype.booking.BookingStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingUpdate {

    private BookingStatus status;
    private LocalDate bookingDate;
    private Integer totalBookedQuantity;
    private String notes;
}
