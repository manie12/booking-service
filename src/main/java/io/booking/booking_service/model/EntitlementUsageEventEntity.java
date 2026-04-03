package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.UsageEventType;
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
@Table("entitlement_usage_events")
public class EntitlementUsageEventEntity {

    @Id
    private UUID id;

    @Column("entitlement_id")
    private UUID entitlementId;

    @Column("event_type")
    private UsageEventType eventType;

    @Column("usage_delta")
    private Integer usageDelta;

    @Column("event_at")
    private OffsetDateTime eventAt;

    @Column("location_code")
    private String locationCode;

    @Column("reference_code")
    private String referenceCode;

    @Column("payload_json")
    private String payloadJson;

    @Column("created_at")
    private OffsetDateTime createdAt;
}