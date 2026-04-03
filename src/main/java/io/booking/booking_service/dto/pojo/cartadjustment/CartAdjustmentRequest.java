package io.booking.booking_service.dto.pojo.cartadjustment;

import io.booking.booking_service.datatype.AdjustmentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CartAdjustmentRequest {

    @NotNull(message = "Cart ID is required")
    private UUID cartId;

    private UUID cartItemId;

    @NotNull(message = "Adjustment type is required")
    private AdjustmentType adjustmentType;

    private String adjustmentName;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String currencyCode;
    private String source;
    private String reasonCode;
}
