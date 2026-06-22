package com.rxpharma.service;

import com.rxpharma.dto.request.PurchaseOrderItemRequest;
import com.rxpharma.entity.*;
import com.rxpharma.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final DrugRepository drugRepository;

    public Page<PurchaseOrder> getAllOrders(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable);
    }

    public PurchaseOrder getOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Purchase order not found with id: " + id));
    }

    @Transactional
    public PurchaseOrder createOrder(Long supplierId, Long orderedById,
                                     BigDecimal totalCost, LocalDate deliveryDate,
                                     List<PurchaseOrderItemRequest> itemRequests) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        User orderedBy = orderedById != null
                ? userRepository.findById(orderedById).orElse(null)
                : null;

        // Auto-calculate total from items if not explicitly provided
        BigDecimal calculatedTotal = itemRequests.stream()
                .map(i -> i.getUnitCost().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PurchaseOrder order = PurchaseOrder.builder()
                .supplier(supplier)
                .orderedBy(orderedBy)
                .totalCost(totalCost != null ? totalCost : calculatedTotal)
                .deliveryDate(deliveryDate)
                .status(PurchaseOrder.Status.DRAFT)
                .build();

        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);

        for (PurchaseOrderItemRequest itemReq : itemRequests) {
            Drug drug = drugRepository.findById(itemReq.getDrugId())
                    .orElseThrow(() -> new RuntimeException(
                            "Drug not found with id: " + itemReq.getDrugId()));

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(savedOrder)
                    .drug(drug)
                    .quantity(itemReq.getQuantity())
                    .unitCost(itemReq.getUnitCost())
                    .build();

            savedOrder.getItems().add(item);
        }

        return purchaseOrderRepository.save(savedOrder);
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

        // Increase drug stock for each item when delivered
        for (PurchaseOrderItem item : order.getItems()) {
            Drug drug = item.getDrug();
            drug.setStockQty(drug.getStockQty() + item.getQuantity());
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