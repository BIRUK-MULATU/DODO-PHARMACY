package com.rxpharma.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PrescriptionResponse {
    private Long id;
    private String patientName;
    private String doctorName;
    private String status;
    private LocalDate issuedDate;
    private String dispensedBy;
    private String notes;
}