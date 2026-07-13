package com.shoes_store.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VariantRequest {

    @NotNull
    private Integer size;

    @NotBlank
    private String color;

    @NotBlank
    private String colorHex;

    @NotNull
    private Integer stock;

}