package com.rxpharma.controller;

import com.rxpharma.dto.request.PrescriptionDrugRequest;
import com.rxpharma.dto.request.PrescriptionRequest;
import com.rxpharma.dto.response.PrescriptionDrugResponse;
import com.rxpharma.dto.response.PrescriptionResponse;
import com.rxpharma.entity.Prescription;
import com.rxpharma.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public ResponseEntity<Page<PrescriptionResponse>> getAllPrescriptions(Pageable pageable) {
        return ResponseEntity.ok(
                prescriptionService.getAllPrescriptions(pageable).map(this::toResponse)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public ResponseEntity<PrescriptionResponse> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(prescriptionService.getPrescriptionById(id)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public ResponseEntity<Page<PrescriptionResponse>> searchByPatientName(
            @RequestParam String patientName,
            Pageable pageable) {
        return ResponseEntity.ok(
                prescriptionService.searchByPatientName(patientName, pageable)
                        .map(this::toResponse)
        );
    }

    @GetMapping("/{id}/drugs")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public ResponseEntity<List<PrescriptionDrugResponse>> getPrescriptionDrugs(
            @PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionDrugs(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @Valid @RequestBody PrescriptionRequest request) {
        Prescription prescription = prescriptionService.createPrescription(
                request.getPatientName(), request.getDoctorName(),
                request.getIssuedDate(), request.getNotes()
        );
        return ResponseEntity.ok(toResponse(prescription));
    }

    @PostMapping("/{id}/drugs")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public ResponseEntity<PrescriptionDrugResponse> addDrugToPrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionDrugRequest request) {
        return ResponseEntity.ok(prescriptionService.addDrugToPrescription(
                id,
                request.getDrugId(),
                request.getQuantity(),
                request.getDosageInstructions()
        ));
    }

    @PatchMapping("/{id}/dispense")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<PrescriptionResponse> dispensePrescription(
            @PathVariable Long id,
            @RequestParam Long pharmacistId) {
        return ResponseEntity.ok(toResponse(
                prescriptionService.dispensePrescription(id, pharmacistId)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public ResponseEntity<PrescriptionResponse> cancelPrescription(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(prescriptionService.cancelPrescription(id)));
    }

    private PrescriptionResponse toResponse(Prescription p) {
        return PrescriptionResponse.builder()
                .id(p.getId())
                .patientName(p.getPatientName())
                .doctorName(p.getDoctorName())
                .status(p.getStatus().name())
                .issuedDate(p.getIssuedDate())
                .dispensedBy(p.getDispensedBy() != null ? p.getDispensedBy().getFullName() : null)
                .notes(p.getNotes())
                .build();
    }
}