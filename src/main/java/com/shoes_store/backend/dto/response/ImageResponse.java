package com.shoes_store.backend.dto.response;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ImageResponse {
    private Long id;
    private String url;
    private String alt;
}
