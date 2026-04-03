package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CheckInResultStatus;
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
@Table("check_ins")
public class CheckInEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("entitlement_id")
    private UUID entitlementId;

    @Column("booking_id")
    private UUID bookingId;

    @Column("booking_item_id")
    private UUID bookingItemId;

    @Column("guest_profile_id")
    private UUID guestProfileId;

    @Column("check_in_at")
    private OffsetDateTime checkInAt;

    @Column("location_code")
    private String locationCode;

    @Column("device_code")
    private String deviceCode;

    @Column("actor_type")
    private ActorType actorType;

    @Column("actor_id")
    private String actorId;

    @Column("result_status")
    private CheckInResultStatus resultStatus;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private OffsetDateTime createdAt;
}