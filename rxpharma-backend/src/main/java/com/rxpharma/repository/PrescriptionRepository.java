package com.rxpharma.repository;

import com.rxpharma.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Page<Prescription> findByStatus(Prescription.Status status, Pageable pageable);
    Page<Prescription> findByPatientNameContainingIgnoreCase(String patientName, Pageable pageable);
}