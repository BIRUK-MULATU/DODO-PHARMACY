package com.rxpharma.dto.request;

import com.rxpharma.entity.Sale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class SaleRequest {

    @NotNull(message = "Cashier ID is required")
    private Long cashierId;

    @NotBlank(message = "Patient name is required")
    private String patientName;

    @NotNull(message = "Payment method is required")
    private Sale.PaymentMethod paymentMethod;

    @NotEmpty(message = "At least one item is required")
    private List<SaleItemRequest> items;

    @Data
    public static class SaleItemRequest {
        @NotNull(message = "Drug ID is required")
        private Long drugId;

        @NotNull(message = "Quantity is required")
        private int quantity;
    }
}