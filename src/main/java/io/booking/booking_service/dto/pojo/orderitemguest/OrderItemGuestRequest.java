package io.booking.booking_service.dto.pojo.orderitemguest;

import io.booking.booking_service.datatype.booking.GuestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class OrderItemGuestRequest {

    @NotNull(message = "Order item ID is required")
    private UUID orderItemId;

    private UUID guestProfileId;

    @NotBlank(message = "Guest first name is required")
    private String guestFirstName;

    @NotBlank(message = "Guest last name is required")
    private String guestLastName;

    private LocalDate guestDateOfBirth;

    @NotNull(message = "Guest type is required")
    private GuestType guestType;

    private Boolean ticketRequired;
    private Boolean waiverRequired;
    private String notes;
}
