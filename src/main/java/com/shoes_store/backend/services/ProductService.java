package com.shoes_store.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.shoes_store.backend.dto.request.ProductRequest;
import com.shoes_store.backend.dto.response.ImageResponse;
import com.shoes_store.backend.dto.response.ProductResponse;
import com.shoes_store.backend.dto.response.VariantResponse;
import com.shoes_store.backend.models.Product;
import com.shoes_store.backend.models.ProductImage;
import com.shoes_store.backend.models.Variant;
import com.shoes_store.backend.repositories.ProductRepository;

import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // ── Get all products ──────────────────────────────────
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── Get by gender ─────────────────────────────────────
    public List<ProductResponse> getByGender(String gender) {
        return productRepository.findByGender(gender)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── Get featured ──────────────────────────────────────
    public List<ProductResponse> getFeatured() {
        return productRepository.findByIsFeaturedTrue()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── Get by slug ───────────────────────────────────────
    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        return toResponse(product);
    }

    // ── Search ────────────────────────────────────────────
    public List<ProductResponse> search(String query) {
        return productRepository
            .findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(query, query)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── Create product ────────────────────────────────────
    @Transactional
    public ProductResponse createProduct(ProductRequest request, MultipartFile image) throws IOException {
        // Save image
        String imageUrl = saveImage(image, request.getSlug());

        // Build product
        Product product = Product.builder()
            .name(request.getName())
            .slug(request.getSlug())
            .description(request.getDescription())
            .price(request.getPrice())
            .compareAtPrice(request.getCompareAtPrice())
            .gender(request.getGender())
            .category(request.getCategory())
            .isFeatured(request.isFeatured())
            .build();

        // Build images
        ProductImage productImage = ProductImage.builder()
            .url(imageUrl)
            .alt(request.getName())
            .product(product)
            .build();
        product.setImages(List.of(productImage));

        // Build variants
        List<Variant> variants = request.getVariants().stream()
            .map(v -> Variant.builder()
                .size(v.getSize())
                .color(v.getColor())
                .colorHex(v.getColorHex())
                .stock(v.getStock())
                .sku(v.getSku())
                .product(product)
                .build())
            .collect(Collectors.toList());
        product.setVariants(variants);

        return toResponse(productRepository.save(product));
    }

    // ── Delete product ────────────────────────────────────
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.deleteById(id);
    }

    // ── Save image to disk ────────────────────────────────
    private String saveImage(MultipartFile file, String slug) throws IOException {
        String uploadDir = "uploads/products/";
        Files.createDirectories(Paths.get(uploadDir));
        String fileName  = slug + "-" + System.currentTimeMillis() +
                           getExtension(file.getOriginalFilename());
        Path filePath    = Paths.get(uploadDir + fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/products/" + fileName;
    }

    private String getExtension(String fileName) {
        if (fileName == null) return ".jpg";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : ".jpg";
    }

    // ── Map to response ───────────────────────────────────
    public ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
            .id(p.getId())
            .name(p.getName())
            .slug(p.getSlug())
            .description(p.getDescription())
            .price(p.getPrice())
            .compareAtPrice(p.getCompareAtPrice())
            .gender(p.getGender())
            .category(p.getCategory())
            .isFeatured(p.isFeatured())
            .createdAt(p.getCreatedAt())
            .images(p.getImages() == null ? List.of() :
                p.getImages().stream()
                    .map(i -> ImageResponse.builder()
                        .id(i.getId())
                        .url(i.getUrl())
                        .alt(i.getAlt())
                        .build())
                    .collect(Collectors.toList()))
            .variants(p.getVariants() == null ? List.of() :
                p.getVariants().stream()
                    .map(v -> VariantResponse.builder()
                        .id(v.getId())
                        .size(v.getSize())
                        .color(v.getColor())
                        .colorHex(v.getColorHex())
                        .stock(v.getStock())
                        .sku(v.getSku())
                        .build())
                    .collect(Collectors.toList()))
            .build();
    }
    
    //Update Product
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, MultipartFile image) throws IOException {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update basic fields
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCompareAtPrice(request.getCompareAtPrice());
        product.setGender(request.getGender());
        product.setCategory(request.getCategory());
        product.setFeatured(request.isFeatured());

        // Update image only if a new one was uploaded
        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(image, request.getSlug());
            product.getImages().clear();
            product.getImages().add(
                ProductImage.builder()
                    .url(imageUrl)
                    .alt(request.getName())
                    .product(product)
                    .build()
            );
        }

        // Replace variants
        product.getVariants().clear();
        if (request.getVariants() != null) {
            List<Variant> variants = request.getVariants().stream()
                .map(v -> Variant.builder()
                    .size(v.getSize())
                    .color(v.getColor())
                    .colorHex(v.getColorHex())
                    .stock(v.getStock())
                    .sku(v.getSku())
                    .product(product)
                    .build())
                .collect(Collectors.toList());
            product.getVariants().addAll(variants);
        }

        return toResponse(productRepository.save(product));
    }
}
