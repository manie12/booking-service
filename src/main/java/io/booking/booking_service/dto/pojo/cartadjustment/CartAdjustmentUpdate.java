package io.booking.booking_service.dto.pojo.cartadjustment;

import io.booking.booking_service.datatype.AdjustmentType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartAdjustmentUpdate {

    private AdjustmentType adjustmentType;
    private String adjustmentName;
    private BigDecimal amount;
    private String currencyCode;
    private String source;
    private String reasonCode;
}
