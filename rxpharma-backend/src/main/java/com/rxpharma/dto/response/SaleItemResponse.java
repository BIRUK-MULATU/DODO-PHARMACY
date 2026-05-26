package com.rxpharma.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class SaleItemResponse {
    private Long id;
    private Long saleId;
    private Long drugId;
    private String drugName;
    private String drugSku;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}