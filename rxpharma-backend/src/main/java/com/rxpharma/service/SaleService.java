package com.rxpharma.service;

import com.rxpharma.dto.response.InvoiceResponse;
import com.rxpharma.dto.response.SaleItemResponse;
import com.rxpharma.entity.*;
import com.rxpharma.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final DrugRepository drugRepository;
    private final UserRepository userRepository;

    public Page<Sale> getAllSales(Pageable pageable) {
        return saleRepository.findAll(pageable);
    }

    public Sale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));
    }

    public Page<Sale> searchByPatientName(String patientName, Pageable pageable) {
        return saleRepository
                .findByPatientNameContainingIgnoreCase(patientName, pageable);
    }

    @Transactional
    public Sale createSale(Long cashierId, String patientName,
                           Sale.PaymentMethod paymentMethod,
                           List<long[]> drugQuantities) {

        User cashier = userRepository.findById(cashierId)
                .orElseThrow(() -> new RuntimeException("Cashier not found"));

        BigDecimal total = BigDecimal.ZERO;

        Sale sale = Sale.builder()
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .cashier(cashier)
                .patientName(patientName)
                .paymentMethod(paymentMethod)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();
        saleRepository.save(sale);

        for (long[] pair : drugQuantities) {
            Long drugId = pair[0];
            int quantity = (int) pair[1];

            Drug drug = drugRepository.findById(drugId)
                    .orElseThrow(() -> new RuntimeException("Drug not found: " + drugId));

            if (drug.getStockQty() < quantity) {
                throw new RuntimeException("Insufficient stock for: " + drug.getName());
            }

            BigDecimal subtotal = drug.getPrice().multiply(BigDecimal.valueOf(quantity));
            total = total.add(subtotal);

            SaleItem item = SaleItem.builder()
                    .sale(sale)
                    .drug(drug)
                    .quantity(quantity)
                    .unitPrice(drug.getPrice())
                    .subtotal(subtotal)
                    .build();
            saleItemRepository.save(item);

            drug.setStockQty(drug.getStockQty() - quantity);
            drugRepository.save(drug);
        }

        BigDecimal tax = total.multiply(BigDecimal.valueOf(0.15));
        sale.setTaxAmount(tax);
        sale.setTotalAmount(total.add(tax));
        return saleRepository.save(sale);
    }

    public List<SaleItemResponse> getSaleItems(Long saleId) {
        Sale sale = getSaleById(saleId);
        return saleItemRepository.findBySaleId(saleId)
                .stream()
                .map(item -> SaleItemResponse.builder()
                        .id(item.getId())
                        .saleId(sale.getId())
                        .drugId(item.getDrug().getId())
                        .drugName(item.getDrug().getName())
                        .drugSku(item.getDrug().getSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());
    }
    public InvoiceResponse getInvoice(Long saleId) {
        Sale sale = getSaleById(saleId);

        List<SaleItemResponse> items = saleItemRepository.findBySaleId(saleId)
                .stream()
                .map(item -> SaleItemResponse.builder()
                        .id(item.getId())
                        .saleId(sale.getId())
                        .drugId(item.getDrug().getId())
                        .drugName(item.getDrug().getName())
                        .drugSku(item.getDrug().getSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(SaleItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return InvoiceResponse.builder()
                .saleId(sale.getId())
                .invoiceNumber(sale.getInvoiceNumber())
                .cashierName(sale.getCashier() != null ?
                        sale.getCashier().getFullName() : null)
                .patientName(sale.getPatientName())
                .paymentMethod(sale.getPaymentMethod().name())
                .saleDate(sale.getSaleDate())
                .items(items)
                .subtotal(subtotal)
                .taxAmount(sale.getTaxAmount())
                .totalAmount(sale.getTotalAmount())
                .build();
    }
}