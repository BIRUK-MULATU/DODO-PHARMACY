package com.rxpharma.repository;

import com.rxpharma.entity.PrescriptionDrug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrescriptionDrugRepository extends JpaRepository<PrescriptionDrug, Long> {
    List<PrescriptionDrug> findByPrescriptionId(Long prescriptionId);
}