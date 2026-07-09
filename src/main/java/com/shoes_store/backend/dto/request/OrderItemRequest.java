package com.shoes_store.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotNull
    private Long productId;

    @NotNull
    private Long variantId;

    @NotBlank
    private String productName;

    @NotNull
    private Integer size;

    @NotBlank
    private String color;

    @NotNull
    private Integer quantity;

    @NotNull
    private Long unitPrice;
}
