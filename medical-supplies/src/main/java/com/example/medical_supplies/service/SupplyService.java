package com.example.medical_supplies.service;

import com.example.medical_supplies.dto.DailyExportDTO;
import com.example.medical_supplies.dto.TopExportDTO;
import com.example.medical_supplies.entity.Supply;
import com.example.medical_supplies.entity.Transaction;
import com.example.medical_supplies.entity.TransactionType;
import com.example.medical_supplies.exception.CustomException;
import com.example.medical_supplies.repository.SupplyRepository;
import com.example.medical_supplies.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplyService {

    private final SupplyRepository supplyRepository;
    private final TransactionRepository transactionRepository;
    private static final Logger historyLogger = LoggerFactory.getLogger("HISTORY_LOGGER");

    // 1. Thêm mới vật tư
    public Supply createSupply(Supply supply) {
        if (supply.getName() == null || supply.getName().trim().isEmpty()) {
            throw new CustomException("Tên vật tư không được để trống", HttpStatus.BAD_REQUEST);
        }
        supply.setQuantity(0); // Luôn bằng 0 khi khởi tạo
        supply.setIsDeleted(false);
        Supply saved = supplyRepository.save(supply);
        log.info("Đã tạo mới vật tư: {} với ID: {}", saved.getName(), saved.getId());
        return saved;
    }

    // 2. Cập nhật thông tin vật tư
    public Supply updateSupply(Long id, Map<String, Object> updates) {
        if (updates.containsKey("id") || updates.containsKey("quantity")) {
            log.warn("Cảnh báo: Client cố tình gửi dữ liệu cấm (id hoặc quantity) khi cập nhật vật tư ID {}", id);
            throw new CustomException("Không được phép cập nhật id hoặc quantity", HttpStatus.BAD_REQUEST);
        }

        Supply existing = supplyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy vật tư với ID: " + id, HttpStatus.NOT_FOUND));

        if (updates.containsKey("name")) existing.setName((String) updates.get("name"));
        if (updates.containsKey("specification")) existing.setSpecification((String) updates.get("specification"));
        if (updates.containsKey("provider")) existing.setProvider((String) updates.get("provider"));

        Supply updated = supplyRepository.save(existing);
        log.info("Đã cập nhật thông tin vật tư ID: {}", id);
        return updated;
    }

    // 3. Xóa mềm
    public void deleteSupply(Long id) {
        Supply existing = supplyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy vật tư hoặc đã bị xóa", HttpStatus.NOT_FOUND));
        existing.setIsDeleted(true);
        supplyRepository.save(existing);
        log.info("Đã xóa mềm vật tư ID: {}", id);
    }

    // 4. Lấy danh sách hiện có
    public List<Supply> getAllActiveSupplies() {
        List<Supply> list = supplyRepository.findByIsDeletedFalse();
        log.debug("Truy vấn danh sách vật tư đang hoạt động. Số lượng bản ghi: {}", list.size());
        return list;
    }

    // 5. Tìm kiếm theo tên
    public List<Supply> searchSupplies(String name) {
        List<Supply> results = supplyRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(name);
        if (results.isEmpty()) {
            log.info("Không tìm thấy vật tư nào khớp với từ khóa: {}", name);
        } else {
            log.info("Tìm thấy {} vật tư với từ khóa: {}", results.size(), name);
        }
        return results;
    }

    // 6. Xuất kho
    public Supply exportSupply(Long id, Integer amount) {
        Supply existing = supplyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy vật tư ID: " + id, HttpStatus.NOT_FOUND));

        if (existing.getQuantity() < amount) {
            log.error("Thất bại khi xuất kho ID {}: Yêu cầu {}, hiện có {}", id, amount, existing.getQuantity());
            throw new CustomException("Số lượng tồn kho không đủ để xuất", HttpStatus.BAD_REQUEST);
        }

        existing.setQuantity(existing.getQuantity() - amount);
        Supply saved = supplyRepository.save(existing);

        Transaction tx = new Transaction();
        tx.setSupply(saved);
        tx.setType(TransactionType.EXPORT);
        tx.setAmount(amount);
        transactionRepository.save(tx);

        log.info("Xuất kho thành công ID {}: -{} (Còn lại: {})", id, amount, saved.getQuantity());
        return saved;
    }

    // 7. Nhập kho
    public Supply importSupply(Long id, Integer amount) {
        Supply existing = supplyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy vật tư ID: " + id, HttpStatus.NOT_FOUND));

        int oldQuantity = existing.getQuantity();
        existing.setQuantity(oldQuantity + amount);
        Supply saved = supplyRepository.save(existing);

        Transaction tx = new Transaction();
        tx.setSupply(saved);
        tx.setType(TransactionType.IMPORT);
        tx.setAmount(amount);
        transactionRepository.save(tx);

        // Ghi log vào file history.log riêng biệt
        historyLogger.info("Nhập kho ID {}, số lượng +{}, tồn cũ {}", id, amount, oldQuantity);
        return saved;
    }

    // 8. Thống kê xuất kho trong ngày
    public List<DailyExportDTO> getDailyExportStatistics() {
        long startTime = System.currentTimeMillis();
        log.info("Bắt đầu chạy thống kê xuất kho trong ngày lúc: {}", LocalDateTime.now());

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        List<DailyExportDTO> stats = transactionRepository.findDailyExports(startOfDay, endOfDay);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Hoàn thành thống kê xuất kho trong ngày. Thời gian thực thi: {} ms", duration);
        return stats;
    }

    // 9. Thống kê vật tư xuất kho nhiều nhất
    public TopExportDTO getTopExportStatistics() {
        List<TopExportDTO> list = transactionRepository.findTopExports();
        if (list.isEmpty()) {
            throw new CustomException("Chưa có dữ liệu giao dịch để thống kê", HttpStatus.NOT_FOUND);
        }
        log.info("Đã thống kê vật tư xuất kho nhiều nhất: {}", list.get(0).getTopSupplyName());
        return list.get(0);
    }
}