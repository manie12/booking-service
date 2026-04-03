package io.booking.booking_service.dto.pojo.accesspass;

import io.booking.booking_service.datatype.booking.PassStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AccessPassUpdate {

    private String barcodeValue;
    private String qrToken;
    private String externalToken;
    private PassStatus status;
    private OffsetDateTime expiresAt;
}
