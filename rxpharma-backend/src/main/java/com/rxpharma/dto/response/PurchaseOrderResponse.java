package com.rxpharma.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PurchaseOrderResponse {
    private Long id;
    private String supplierName;
    private String orderedBy;
    private String status;
    private BigDecimal totalCost;
    private LocalDateTime orderDate;
    private LocalDate deliveryDate;
    private List<PurchaseOrderItemResponse> items;
}