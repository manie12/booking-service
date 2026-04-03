package io.booking.booking_service.dto.pojo.bookingitem;

import io.booking.booking_service.datatype.booking.BookingStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class BookingItemResponse {

    private UUID id;
    private UUID bookingId;
    private UUID orderItemId;
    private UUID scheduleInstanceId;
    private UUID capacityPoolId;
    private UUID productId;
    private UUID productVariantId;
    private UUID offerId;
    private Integer quantity;
    private Integer unitCount;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private BookingStatus status;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
