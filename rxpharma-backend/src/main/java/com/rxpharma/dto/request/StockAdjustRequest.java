package com.rxpharma.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustRequest {

    @NotNull(message = "Quantity is required")
    private int quantity;

    @NotNull(message = "Adjustment type is required")
    private AdjustmentType type;

    private String reason;

    public enum AdjustmentType {
        ADD, SUBTRACT, SET
    }
}