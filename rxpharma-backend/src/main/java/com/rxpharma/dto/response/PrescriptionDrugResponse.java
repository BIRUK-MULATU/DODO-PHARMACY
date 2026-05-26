package com.rxpharma.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrescriptionDrugResponse {
    private Long id;
    private Long prescriptionId;
    private Long drugId;
    private String drugName;
    private String drugSku;
    private int quantity;
    private String dosageInstructions;
}