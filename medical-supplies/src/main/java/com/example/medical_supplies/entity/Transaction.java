package com.example.medical_supplies.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supply_id", nullable = false)
    private Supply supply;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Integer amount;
    private LocalDateTime transactionDate = LocalDateTime.now();
}
