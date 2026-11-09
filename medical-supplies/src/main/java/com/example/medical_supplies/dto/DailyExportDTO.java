package com.example.medical_supplies.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyExportDTO {
    private Long supplyId;
    private String supplyName;
    private Long totalExportedToday;
}
