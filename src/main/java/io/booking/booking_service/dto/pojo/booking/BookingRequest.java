package io.booking.booking_service.dto.pojo.booking;

import io.booking.booking_service.datatype.booking.BookingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookingRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotBlank(message = "Booking number is required")
    private String bookingNumber;

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    private UUID channelId;
    private BookingStatus status;

    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;

    @NotBlank(message = "Currency code is required")
    private String currencyCode;

    private Integer totalBookedQuantity;
    private String notes;
}
