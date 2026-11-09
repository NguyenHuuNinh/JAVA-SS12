package com.example.medical_supplies.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "supplies")
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String specification; // Quy cách / Đơn vị tính
    private String provider;      // Hãng / Nhà sản xuất
    private Integer quantity = 0; // Mặc định bằng 0
    private Boolean isDeleted = false; // Xóa mềm
}