package com.shoes_store.backend.dto.response;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private Long totalRevenue;
    private Long totalOrders;
    private Long pendingOrders;
    private Long totalProducts;
    private List<OrderResponse> recentOrders;
}
