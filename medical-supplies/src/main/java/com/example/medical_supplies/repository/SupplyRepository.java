package com.example.medical_supplies.repository;

import com.example.medical_supplies.entity.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
    List<Supply> findByIsDeletedFalse();
    Optional<Supply> findByIdAndIsDeletedFalse(Long id);
    List<Supply> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name);
}
