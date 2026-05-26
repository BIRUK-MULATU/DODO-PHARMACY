package com.rxpharma.repository;

import com.rxpharma.entity.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface DrugRepository extends JpaRepository<Drug, Long> {

    boolean existsBySku(String sku);

    // Search & filter endpoint with multiple params
    @Query("""
        SELECT d FROM Drug d
        WHERE (:category IS NULL OR d.category = :category)
        AND (:supplierId IS NULL OR d.supplier.id = :supplierId)
        AND (:lowStock IS NULL OR (:lowStock = true AND d.stockQty < 10))
        AND (:expiringBefore IS NULL OR d.expiryDate <= :expiringBefore)
    """)
    Page<Drug> searchDrugs(
            @Param("category") String category,
            @Param("supplierId") Long supplierId,
            @Param("lowStock") Boolean lowStock,
            @Param("expiringBefore") LocalDate expiringBefore,
            Pageable pageable
    );
}