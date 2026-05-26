package com.rxpharma.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InvoiceResponse {
    private Long saleId;
    private String invoiceNumber;
    private String cashierName;
    private String patientName;
    private String paymentMethod;
    private LocalDateTime saleDate;
    private List<SaleItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
}