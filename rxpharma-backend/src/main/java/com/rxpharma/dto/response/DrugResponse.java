package com.rxpharma.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DrugResponse {
    private Long id;
    private String name;
    private String sku;
    private String category;
    private BigDecimal price;
    private int stockQty;
    private LocalDate expiryDate;
    private Long supplierId;
    private String supplierName;
    private boolean lowStock;
    private boolean expiringSoon;
}