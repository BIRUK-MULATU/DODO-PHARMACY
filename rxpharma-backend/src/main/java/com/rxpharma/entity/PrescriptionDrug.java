package com.rxpharma.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prescription_drugs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrescriptionDrug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "dosage_instructions", columnDefinition = "TEXT")
    private String dosageInstructions;
}