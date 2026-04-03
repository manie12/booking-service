package io.booking.booking_service.dto.pojo.guestprofile;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class GuestProfileResponse {

    private UUID id;
    private UUID tenantId;
    private UUID customerId;
    private String firstName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String nationalityCode;
    private String documentType;
    private String documentNumber;
    private String email;
    private String phoneNumber;
    private String specialRequirements;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
