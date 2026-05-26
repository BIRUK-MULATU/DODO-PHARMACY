package com.rxpharma.repository;

import com.rxpharma.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    boolean existsByInvoiceNumber(String invoiceNumber);
    Page<Sale> findBySaleDateBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<Sale> findByPatientNameContainingIgnoreCase(String patientName, Pageable pageable);
}