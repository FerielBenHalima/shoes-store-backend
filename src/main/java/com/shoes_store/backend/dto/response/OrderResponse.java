package com.shoes_store.backend.dto.response;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String fullName;
    private String phone;
    private String city;
    private String address;
    private String notes;
    private Long total;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
