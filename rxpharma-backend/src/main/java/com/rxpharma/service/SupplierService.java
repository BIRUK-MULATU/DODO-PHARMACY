package com.rxpharma.service;

import com.rxpharma.entity.Supplier;
import com.rxpharma.dto.request.SupplierRequest;
import com.rxpharma.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public List<Supplier> getSuppliersByType(Supplier.SupplierType supplierType) {
        return supplierRepository.findBySupplierType(supplierType);
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
    }

    public Supplier createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use: " + request.getEmail());
        }
        Supplier supplier = Supplier.builder()
                .companyName(request.getCompanyName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(Supplier.Status.ACTIVE)
                .supplierType(request.getSupplierType() != null
                        ? request.getSupplierType() : Supplier.SupplierType.WHOLESALER)
                .address(request.getAddress())
                .build();
        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = getSupplierById(id);
        supplier.setCompanyName(request.getCompanyName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        if (request.getStatus() != null) supplier.setStatus(request.getStatus());
        if (request.getSupplierType() != null) supplier.setSupplierType(request.getSupplierType());
        if (request.getAddress() != null) supplier.setAddress(request.getAddress());
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
    }
}