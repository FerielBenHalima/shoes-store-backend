package com.shoes_store.backend.dto.response;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class VariantResponse {
    private Long id;
    private Integer size;
    private String color;
    private String colorHex;
    private Integer stock;
}