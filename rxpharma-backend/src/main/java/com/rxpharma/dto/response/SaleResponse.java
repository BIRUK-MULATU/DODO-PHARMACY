package com.rxpharma.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SaleResponse {
    private Long id;
    private String invoiceNumber;
    private String cashierName;
    private String patientName;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String paymentMethod;
    private LocalDateTime saleDate;
}