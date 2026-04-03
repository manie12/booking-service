package io.booking.booking_service.dto.pojo.cartadjustment;

import io.booking.booking_service.datatype.AdjustmentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CartAdjustmentResponse {

    private UUID id;
    private UUID cartId;
    private UUID cartItemId;
    private AdjustmentType adjustmentType;
    private String adjustmentName;
    private BigDecimal amount;
    private String currencyCode;
    private String source;
    private String reasonCode;
    private OffsetDateTime createdAt;
}
