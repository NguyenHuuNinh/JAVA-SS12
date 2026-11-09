package com.example.medical_supplies.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopExportDTO {
    private String topSupplyName;
    private Long totalExportQuantity;
}
