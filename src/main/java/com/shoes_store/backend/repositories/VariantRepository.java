package com.shoes_store.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.shoes_store.backend.models.Variant;
import java.util.List;

public interface VariantRepository extends JpaRepository<Variant, Long> {
    List<Variant> findByProductId(Long productId);
}
