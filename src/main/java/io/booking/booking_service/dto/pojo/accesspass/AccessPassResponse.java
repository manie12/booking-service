package io.booking.booking_service.dto.pojo.accesspass;

import io.booking.booking_service.datatype.booking.PassStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AccessPassResponse {

    private UUID id;
    private UUID entitlementId;
    private String passNumber;
    private String passType;
    private String barcodeValue;
    private String qrToken;
    private String externalToken;
    private PassStatus status;
    private OffsetDateTime issuedAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
}
