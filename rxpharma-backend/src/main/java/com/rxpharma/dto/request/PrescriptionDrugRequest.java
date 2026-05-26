package com.rxpharma.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionDrugRequest {

    @NotNull(message = "Drug ID is required")
    private Long drugId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private String dosageInstructions;
}