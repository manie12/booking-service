package io.booking.booking_service.dto.pojo.accesspass;

import io.booking.booking_service.datatype.booking.PassStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AccessPassRequest {

    @NotNull(message = "Entitlement ID is required")
    private UUID entitlementId;

    @NotBlank(message = "Pass number is required")
    private String passNumber;

    @NotBlank(message = "Pass type is required")
    private String passType;

    private String barcodeValue;
    private String qrToken;
    private String externalToken;
    private PassStatus status;
    private OffsetDateTime issuedAt;
    private OffsetDateTime expiresAt;
}
