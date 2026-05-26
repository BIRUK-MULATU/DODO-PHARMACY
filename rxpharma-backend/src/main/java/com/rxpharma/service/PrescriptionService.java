package com.rxpharma.service;

import com.rxpharma.dto.response.PrescriptionDrugResponse;
import com.rxpharma.entity.*;
import com.rxpharma.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDrugRepository prescriptionDrugRepository;
    private final DrugRepository drugRepository;
    private final UserRepository userRepository;

    public Page<Prescription> getAllPrescriptions(Pageable pageable) {
        return prescriptionRepository.findAll(pageable);
    }

    public Prescription getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found with id: " + id));
    }

    public Page<Prescription> searchByPatientName(String patientName, Pageable pageable) {
        return prescriptionRepository
                .findByPatientNameContainingIgnoreCase(patientName, pageable);
    }

    public Prescription createPrescription(String patientName, String doctorName,
                                           LocalDate issuedDate, String notes) {
        Prescription prescription = Prescription.builder()
                .patientName(patientName)
                .doctorName(doctorName)
                .issuedDate(issuedDate)
                .notes(notes)
                .status(Prescription.Status.PENDING)
                .build();
        return prescriptionRepository.save(prescription);
    }

    @Transactional
    public Prescription dispensePrescription(Long prescriptionId, Long pharmacistId) {
        Prescription prescription = getPrescriptionById(prescriptionId);

        if (prescription.getStatus() != Prescription.Status.PENDING) {
            throw new RuntimeException("Only PENDING prescriptions can be dispensed");
        }

        User pharmacist = userRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

        for (PrescriptionDrug pd : prescription.getPrescriptionDrugs()) {
            Drug drug = pd.getDrug();
            if (drug.getStockQty() < pd.getQuantity()) {
                throw new RuntimeException("Insufficient stock for drug: " + drug.getName());
            }
            drug.setStockQty(drug.getStockQty() - pd.getQuantity());
            drugRepository.save(drug);
        }

        prescription.setStatus(Prescription.Status.DISPENSED);
        prescription.setDispensedBy(pharmacist);
        return prescriptionRepository.save(prescription);
    }

    public Prescription cancelPrescription(Long id) {
        Prescription prescription = getPrescriptionById(id);
        if (prescription.getStatus() != Prescription.Status.PENDING) {
            throw new RuntimeException("Only PENDING prescriptions can be cancelled");
        }
        prescription.setStatus(Prescription.Status.CANCELLED);
        return prescriptionRepository.save(prescription);
    }

    public List<PrescriptionDrugResponse> getPrescriptionDrugs(Long prescriptionId) {
        Prescription prescription = getPrescriptionById(prescriptionId);
        return prescriptionDrugRepository.findByPrescriptionId(prescriptionId)
                .stream()
                .map(pd -> PrescriptionDrugResponse.builder()
                        .id(pd.getId())
                        .prescriptionId(prescription.getId())
                        .drugId(pd.getDrug().getId())
                        .drugName(pd.getDrug().getName())
                        .drugSku(pd.getDrug().getSku())
                        .quantity(pd.getQuantity())
                        .dosageInstructions(pd.getDosageInstructions())
                        .build())
                .collect(Collectors.toList());
    }

    public PrescriptionDrugResponse addDrugToPrescription(Long prescriptionId,
                                                          Long drugId,
                                                          int quantity,
                                                          String dosageInstructions) {
        Prescription prescription = getPrescriptionById(prescriptionId);

        if (prescription.getStatus() != Prescription.Status.PENDING) {
            throw new RuntimeException("Cannot add drugs to a non-PENDING prescription");
        }

        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new RuntimeException("Drug not found with id: " + drugId));

        PrescriptionDrug pd = PrescriptionDrug.builder()
                .prescription(prescription)
                .drug(drug)
                .quantity(quantity)
                .dosageInstructions(dosageInstructions)
                .build();

        prescriptionDrugRepository.save(pd);

        return PrescriptionDrugResponse.builder()
                .id(pd.getId())
                .prescriptionId(prescription.getId())
                .drugId(drug.getId())
                .drugName(drug.getName())
                .drugSku(drug.getSku())
                .quantity(quantity)
                .dosageInstructions(dosageInstructions)
                .build();
    }
}