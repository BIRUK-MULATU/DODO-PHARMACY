package com.rxpharma.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderRequest {

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    private Long orderedById;

    private BigDecimal totalCost;

    private LocalDate deliveryDate;

    private String notes;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<PurchaseOrderItemRequest> items;
}