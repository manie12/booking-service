package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("reschedules")
public class RescheduleEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("booking_id")
    private UUID bookingId;

    @Column("booking_item_id")
    private UUID bookingItemId;

    @Column("old_schedule_instance_id")
    private UUID oldScheduleInstanceId;

    @Column("new_schedule_instance_id")
    private UUID newScheduleInstanceId;

    @Column("old_start_at")
    private OffsetDateTime oldStartAt;

    @Column("new_start_at")
    private OffsetDateTime newStartAt;

    @Column("reschedule_fee_amount")
    private BigDecimal rescheduleFeeAmount;

    @Column("currency_code")
    private String currencyCode;

    @Column("reason_code")
    private String reasonCode;

    @Column("reason_text")
    private String reasonText;

    @Column("actor_type")
    private ActorType actorType;

    @Column("actor_id")
    private String actorId;

    @Column("status")
    private CancellationStatus status;

    @Column("requested_at")
    private OffsetDateTime requestedAt;

    @Column("completed_at")
    private OffsetDateTime completedAt;

    @Column("created_at")
    private OffsetDateTime createdAt;
}