package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.EntitlementStatus;
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
@Table("entitlements")
public class EntitlementEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("entitlement_number")
    private String entitlementNumber;

    @Column("order_id")
    private UUID orderId;

    @Column("order_item_id")
    private UUID orderItemId;

    @Column("booking_id")
    private UUID bookingId;

    @Column("booking_item_id")
    private UUID bookingItemId;

    @Column("guest_profile_id")
    private UUID guestProfileId;

    @Column("entitlement_type")
    private String entitlementType;

    @Column("status")
    private EntitlementStatus status;

    @Column("issued_at")
    private OffsetDateTime issuedAt;

    @Column("valid_from")
    private OffsetDateTime validFrom;

    @Column("valid_to")
    private OffsetDateTime validTo;

    @Column("usage_limit")
    private Integer usageLimit;

    @Column("usage_count")
    private Integer usageCount;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}