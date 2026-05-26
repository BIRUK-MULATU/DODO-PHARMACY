package com.rxpharma.service;

import com.rxpharma.entity.Supplier;
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

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
    }

    public Supplier createSupplier(String companyName, String contactPerson,
                                   String email, String phone) {
        if (supplierRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use: " + email);
        }
        Supplier supplier = Supplier.builder()
                .companyName(companyName)
                .contactPerson(contactPerson)
                .email(email)
                .phone(phone)
                .status(Supplier.Status.ACTIVE)
                .build();
        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(Long id, String companyName, String contactPerson,
                                   String email, String phone, Supplier.Status status) {
        Supplier supplier = getSupplierById(id);
        supplier.setCompanyName(companyName);
        supplier.setContactPerson(contactPerson);
        supplier.setEmail(email);
        supplier.setPhone(phone);
        supplier.setStatus(status);
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
    }
}