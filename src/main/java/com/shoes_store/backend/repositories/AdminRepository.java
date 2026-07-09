package com.shoes_store.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.shoes_store.backend.models.Admin;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
}