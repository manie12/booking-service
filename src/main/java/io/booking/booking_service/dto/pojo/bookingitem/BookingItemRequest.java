package io.booking.booking_service.dto.pojo.bookingitem;

import io.booking.booking_service.datatype.booking.BookingStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class BookingItemRequest {

    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    @NotNull(message = "Order item ID is required")
    private UUID orderItemId;

    @NotNull(message = "Schedule instance ID is required")
    private UUID scheduleInstanceId;

    private UUID capacityPoolId;
    private UUID productId;
    private UUID productVariantId;
    private UUID offerId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private Integer unitCount;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private BookingStatus status;
    private String notes;
}
