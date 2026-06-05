package com.rxpharma.controller;

import com.rxpharma.dto.request.SupplierRequest;
import com.rxpharma.dto.response.SupplierResponse;
import com.rxpharma.entity.Supplier;
import com.rxpharma.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER_MANAGER','PHARMACIST')")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        List<SupplierResponse> suppliers = supplierService.getAllSuppliers()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER_MANAGER')")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(supplierService.getSupplierById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER_MANAGER')")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody SupplierRequest request) {
        Supplier supplier = supplierService.createSupplier(
                request.getCompanyName(), request.getContactPerson(),
                request.getEmail(), request.getPhone()
        );
        return ResponseEntity.ok(toResponse(supplier));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER_MANAGER')")
    public ResponseEntity<SupplierResponse> updateSupplier(@PathVariable Long id,
                                                           @Valid @RequestBody SupplierRequest request) {
        Supplier supplier = supplierService.updateSupplier(
                id, request.getCompanyName(), request.getContactPerson(),
                request.getEmail(), request.getPhone(), request.getStatus()
        );
        return ResponseEntity.ok(toResponse(supplier));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .companyName(supplier.getCompanyName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .status(supplier.getStatus().name())
                .createdAt(supplier.getCreatedAt())
                .build();
    }
}