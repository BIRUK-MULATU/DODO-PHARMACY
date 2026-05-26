package com.rxpharma.controller;

import com.rxpharma.dto.request.SaleRequest;
import com.rxpharma.dto.response.InvoiceResponse;
import com.rxpharma.dto.response.SaleItemResponse;
import com.rxpharma.dto.response.SaleResponse;
import com.rxpharma.entity.Sale;
import com.rxpharma.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<Page<SaleResponse>> getAllSales(Pageable pageable) {
        return ResponseEntity.ok(
                saleService.getAllSales(pageable).map(this::toResponse)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<SaleResponse> getSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(saleService.getSaleById(id)));
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<List<SaleItemResponse>> getSaleItems(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSaleItems(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<Page<SaleResponse>> searchByPatientName(
            @RequestParam String patientName,
            Pageable pageable) {
        return ResponseEntity.ok(
                saleService.searchByPatientName(patientName, pageable)
                        .map(this::toResponse)
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleRequest request) {
        List<long[]> drugQuantities = request.getItems().stream()
                .map(item -> new long[]{item.getDrugId(), item.getQuantity()})
                .collect(Collectors.toList());

        Sale sale = saleService.createSale(
                request.getCashierId(),
                request.getPatientName(),
                request.getPaymentMethod(),
                drugQuantities
        );
        return ResponseEntity.ok(toResponse(sale));
    }

    private SaleResponse toResponse(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .invoiceNumber(sale.getInvoiceNumber())
                .cashierName(sale.getCashier() != null ? sale.getCashier().getFullName() : null)
                .patientName(sale.getPatientName())
                .totalAmount(sale.getTotalAmount())
                .taxAmount(sale.getTaxAmount())
                .paymentMethod(sale.getPaymentMethod().name())
                .saleDate(sale.getSaleDate())
                .build();
    }
    @GetMapping("/{id}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getInvoice(id));
    }
}