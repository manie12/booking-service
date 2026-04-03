package io.booking.booking_service.dto.pojo.customer;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CustomerResponse {

    private UUID id;
    private UUID tenantId;
    private String customerNumber;
    private String externalReference;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String countryCode;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
