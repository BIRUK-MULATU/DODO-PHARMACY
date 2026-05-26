package com.rxpharma.controller;

import com.rxpharma.dto.request.DeliverOrderRequest;
import com.rxpharma.dto.request.PurchaseOrderRequest;
import com.rxpharma.dto.response.PurchaseOrderResponse;
import com.rxpharma.entity.PurchaseOrder;
import com.rxpharma.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER_MANAGER')")
    public ResponseEntity<Page<PurchaseOrderResponse>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(
                purchaseOrderService.getAllOrders(pageable).map(this::toResponse)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER_MANAGER')")
    public ResponseEntity<PurchaseOrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(purchaseOrderService.getOrderById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER_MANAGER')")
    public ResponseEntity<PurchaseOrderResponse> createOrder(
            @Valid @RequestBody PurchaseOrderRequest request) {
        PurchaseOrder order = purchaseOrderService.createOrder(
                request.getSupplierId(), request.getOrderedById(),
                request.getTotalCost(), request.getDeliveryDate()
        );
        return ResponseEntity.ok(toResponse(order));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER_MANAGER')")
    public ResponseEntity<PurchaseOrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam PurchaseOrder.Status status) {
        return ResponseEntity.ok(
                toResponse(purchaseOrderService.updateOrderStatus(id, status)));
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER_MANAGER')")
    public ResponseEntity<PurchaseOrderResponse> deliverOrder(
            @PathVariable Long id,
            @Valid @RequestBody DeliverOrderRequest request) {
        return ResponseEntity.ok(
                toResponse(purchaseOrderService.deliverOrder(
                        id, request.getDeliveryDate())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        purchaseOrderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder order) {
        return PurchaseOrderResponse.builder()
                .id(order.getId())
                .supplierName(order.getSupplier().getCompanyName())
                .orderedBy(order.getOrderedBy() != null ?
                        order.getOrderedBy().getFullName() : null)
                .status(order.getStatus().name())
                .totalCost(order.getTotalCost())
                .orderDate(order.getOrderDate())
                .deliveryDate(order.getDeliveryDate())
                .build();
    }
}