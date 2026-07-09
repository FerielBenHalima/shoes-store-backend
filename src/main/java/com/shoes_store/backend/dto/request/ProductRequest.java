package com.shoes_store.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ProductRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private String description;

    @NotNull
    private Long price;

    private Long compareAtPrice;

    @NotBlank
    private String gender;

    @NotBlank
    private String category;
    
    @JsonProperty("isFeatured")
    private boolean isFeatured;

    private List<VariantRequest> variants;
}