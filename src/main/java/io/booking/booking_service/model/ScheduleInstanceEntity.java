package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.ScheduleInstanceStatus;
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
@Table("schedule_instances")
public class ScheduleInstanceEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("product_id")
    private UUID productId;

    @Column("product_variant_id")
    private UUID productVariantId;

    @Column("instance_code")
    private String instanceCode;

    @Column("instance_name")
    private String instanceName;

    @Column("start_at")
    private OffsetDateTime startAt;

    @Column("end_at")
    private OffsetDateTime endAt;

    @Column("timezone")
    private String timezone;

    @Column("venue_code")
    private String venueCode;

    @Column("resource_reference")
    private String resourceReference;

    @Column("status")
    private ScheduleInstanceStatus status;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}