package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.PassStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("access_passes")
public class AccessPassEntity {

    @Id
    private UUID id;

    @Column("entitlement_id")
    private UUID entitlementId;

    @Column("pass_number")
    private String passNumber;

    @Column("pass_type")
    private String passType;

    @Column("barcode_value")
    private String barcodeValue;

    @Column("qr_token")
    private String qrToken;

    @Column("external_token")
    private String externalToken;

    @Column("status")
    private PassStatus status;

    @Column("issued_at")
    private OffsetDateTime issuedAt;

    @Column("expires_at")
    private OffsetDateTime expiresAt;

    @Column("created_at")
    private OffsetDateTime createdAt;
}