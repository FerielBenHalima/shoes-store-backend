package com.shoes_store.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.shoes_store.backend.models.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    List<Product> findByGender(String gender);
    List<Product> findByIsFeaturedTrue();
    List<Product> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String category);
}