package com.example.medical_supplies.controller;

import com.example.medical_supplies.dto.*;
import com.example.medical_supplies.entity.Supply;
import com.example.medical_supplies.service.SupplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/supplies")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyService supplyService;

    @PostMapping
    public ResponseEntity<ApiResponse<Supply>> create(@RequestBody Supply supply) {
        Supply created = supplyService.createSupply(supply);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Tạo vật tư thành công", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Supply>> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Supply updated = supplyService.updateSupply(id, updates);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật vật tư thành công", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplyService.deleteSupply(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Supply>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách thành công", supplyService.getAllActiveSupplies()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Supply>>> search(@RequestParam("name") String name) {
        return ResponseEntity.ok(ApiResponse.success(200, "Tìm kiếm hoàn tất", supplyService.searchSupplies(name)));
    }

    @PatchMapping("/{id}/export")
    public ResponseEntity<ApiResponse<Supply>> exportSupply(@PathVariable Long id, @RequestBody AmountRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, "Xuất kho thành công", supplyService.exportSupply(id, request.getAmount())));
    }

    @PatchMapping("/{id}/import")
    public ResponseEntity<ApiResponse<Supply>> importSupply(@PathVariable Long id, @RequestBody AmountRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, "Nhập kho thành công", supplyService.importSupply(id, request.getAmount())));
    }

    @GetMapping("/statistics/daily-export")
    public ResponseEntity<ApiResponse<List<DailyExportDTO>>> getDailyExport() {
        return ResponseEntity.ok(ApiResponse.success(200, "Thống kê xuất kho trong ngày", supplyService.getDailyExportStatistics()));
    }

    @GetMapping("/statistics/top-export")
    public ResponseEntity<ApiResponse<TopExportDTO>> getTopExport() {
        return ResponseEntity.ok(ApiResponse.success(200, "Thống kê vật tư xuất nhiều nhất", supplyService.getTopExportStatistics()));
    }
}