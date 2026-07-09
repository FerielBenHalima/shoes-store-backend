package com.shoes_store.backend.dto.response;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long price;
    private Long compareAtPrice;
    private String gender;
    private String category;
    private boolean isFeatured;
    private LocalDateTime createdAt;
    private List<ImageResponse> images;
    private List<VariantResponse> variants;
}