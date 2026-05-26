package com.rxpharma.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {
    private long totalUsers;
    private long totalDrugs;
    private long totalSuppliers;
    private long totalSales;
    private long totalPrescriptions;
    private long totalPurchaseOrders;
    private long lowStockCount;
    private long expiringSoonCount;
    private long pendingPrescriptions;
    private long pendingOrders;
}