package com.example.foxticket.repositories;

import com.example.foxticket.models.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
    Optional<ProductType> findById(Long id);

    Optional<ProductType> findByName(String name);
}