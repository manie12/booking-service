package io.booking.booking_service.dto.pojo.booking;

import io.booking.booking_service.datatype.booking.BookingStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class BookingResponse {

    private UUID id;
    private UUID tenantId;
    private String bookingNumber;
    private UUID orderId;
    private UUID customerId;
    private UUID channelId;
    private BookingStatus status;
    private LocalDate bookingDate;
    private String currencyCode;
    private Integer totalBookedQuantity;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
