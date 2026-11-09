package com.example.medical_supplies.repository;


import com.example.medical_supplies.dto.DailyExportDTO;
import com.example.medical_supplies.dto.TopExportDTO;
import com.example.medical_supplies.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT new com.example.medical_supplies.dto.DailyExportDTO(t.supply.id, t.supply.name, SUM(t.amount)) " +
            "FROM Transaction t WHERE t.type = 'EXPORT' AND t.transactionDate >= :startOfDay AND t.transactionDate <= :endOfDay " +
            "GROUP BY t.supply.id, t.supply.name")
    List<DailyExportDTO> findDailyExports(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT new com.example.medical_supplies.dto.TopExportDTO(t.supply.name, SUM(t.amount)) " +
            "FROM Transaction t WHERE t.type = 'EXPORT' " +
            "GROUP BY t.supply.id, t.supply.name ORDER BY SUM(t.amount) DESC")
    List<TopExportDTO> findTopExports();
}
