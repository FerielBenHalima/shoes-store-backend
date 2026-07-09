package com.shoes_store.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.shoes_store.backend.dto.request.ProductRequest;
import com.shoes_store.backend.dto.response.ProductResponse;
import com.shoes_store.backend.services.ProductService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Public endpoints
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/gender/{gender}")
    public ResponseEntity<List<ProductResponse>> getByGender(@PathVariable String gender) {
        return ResponseEntity.ok(productService.getByGender(gender));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeatured() {
        return ResponseEntity.ok(productService.getFeatured());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(productService.search(q));
    }

    // Admin endpoints
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductResponse> create(
        @Valid @RequestPart("product") ProductRequest request,
        @RequestPart("image") MultipartFile image
    ) throws IOException {
        return ResponseEntity.ok(productService.createProduct(request, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ProductResponse> update(
        @PathVariable Long id,
        @RequestPart("product") ProductRequest request,
        @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        return ResponseEntity.ok(productService.updateProduct(id, request, image));
    }
}