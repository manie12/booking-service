package io.booking.booking_service.dto.pojo.bookingitem;

import io.booking.booking_service.datatype.booking.BookingStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class BookingItemUpdate {

    private Integer quantity;
    private Integer unitCount;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private BookingStatus status;
    private String notes;
}
