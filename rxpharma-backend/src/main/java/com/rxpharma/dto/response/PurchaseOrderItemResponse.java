package com.rxpharma.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PurchaseOrderItemResponse {
    private Long id;
    private Long drugId;
    private String drugName;
    private int quantity;
    private BigDecimal unitCost;
    private BigDecimal subtotal;
}