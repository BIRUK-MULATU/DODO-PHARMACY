package com.rxpharma.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItemRequest {

    @NotNull(message = "Drug is required")
    private Long drugId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotNull(message = "Unit cost is required")
    private BigDecimal unitCost;
}