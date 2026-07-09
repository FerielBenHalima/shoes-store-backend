package com.shoes_store.backend.dto.response;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private Integer size;
    private String color;
    private Integer quantity;
    private Long unitPrice;
}