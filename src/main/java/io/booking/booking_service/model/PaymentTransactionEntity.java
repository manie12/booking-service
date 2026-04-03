package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.PaymentStatus;
import io.booking.booking_service.datatype.booking.PaymentTransactionType;
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
@Table("payment_transactions")
public class PaymentTransactionEntity {

    @Id
    private UUID id;

    @Column("payment_id")
    private UUID paymentId;

    @Column("transaction_reference")
    private String transactionReference;

    @Column("provider_transaction_id")
    private String providerTransactionId;

    @Column("transaction_type")
    private PaymentTransactionType transactionType;

    @Column("status")
    private PaymentStatus status;

    @Column("amount")
    private BigDecimal amount;

    @Column("currency_code")
    private String currencyCode;

    @Column("provider_response_code")
    private String providerResponseCode;

    @Column("provider_response_message")
    private String providerResponseMessage;

    @Column("payload_json")
    private String payloadJson;

    @Column("processed_at")
    private OffsetDateTime processedAt;

    @Column("created_at")
    private OffsetDateTime createdAt;
}