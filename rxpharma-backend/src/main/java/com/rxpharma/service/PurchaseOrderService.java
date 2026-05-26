package com.rxpharma.service;

import com.rxpharma.entity.*;
import com.rxpharma.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    public Page<PurchaseOrder> getAllOrders(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable);
    }

    public PurchaseOrder getOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Purchase order not found with id: " + id));
    }

    public PurchaseOrder createOrder(Long supplierId, Long orderedById,
                                     BigDecimal totalCost, LocalDate deliveryDate) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        User orderedBy = userRepository.findById(orderedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PurchaseOrder order = PurchaseOrder.builder()
                .supplier(supplier)
                .orderedBy(orderedBy)
                .totalCost(totalCost)
                .deliveryDate(deliveryDate)
                .status(PurchaseOrder.Status.DRAFT)
                .build();
        return purchaseOrderRepository.save(order);
    }

    public PurchaseOrder updateOrderStatus(Long id, PurchaseOrder.Status status) {
        PurchaseOrder order = getOrderById(id);
        order.setStatus(status);
        return purchaseOrderRepository.save(order);
    }

    @Transactional
    public PurchaseOrder deliverOrder(Long id, LocalDate deliveryDate) {
        PurchaseOrder order = getOrderById(id);

        if (order.getStatus() == PurchaseOrder.Status.DELIVERED) {
            throw new com.rxpharma.exception.BadRequestException(
                    "Order is already delivered");
        }

        if (order.getStatus() == PurchaseOrder.Status.CANCELLED) {
            throw new com.rxpharma.exception.BadRequestException(
                    "Cannot deliver a cancelled order");
        }

        order.setStatus(PurchaseOrder.Status.DELIVERED);
        order.setDeliveryDate(deliveryDate);
        return purchaseOrderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        if (!purchaseOrderRepository.existsById(id)) {
            throw new RuntimeException("Purchase order not found with id: " + id);
        }
        purchaseOrderRepository.deleteById(id);
    }
}