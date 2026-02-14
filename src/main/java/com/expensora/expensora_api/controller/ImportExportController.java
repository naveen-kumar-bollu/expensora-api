package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.ImportHistoryDto;
import com.expensora.expensora_api.dto.ImportPreviewDto;
import com.expensora.expensora_api.entity.ImportFormat;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.ImportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/import-export")
@Tag(name = "Import/Export", description = "Data import and export APIs")
public class ImportExportController {

    @Autowired
    private ImportExportService importExportService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/preview")
    @Operation(summary = "Preview import", description = "Preview CSV file before importing")
    public ResponseEntity<ImportPreviewDto> previewImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") ImportFormat format) {
        ImportPreviewDto preview = importExportService.previewImport(file, format);
        return ResponseEntity.ok(preview);
    }

    @PostMapping("/import")
    @Operation(summary = "Import transactions", description = "Import transactions from CSV file")
    public ResponseEntity<ImportHistoryDto> importTransactions(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") ImportFormat format,
            @RequestBody Map<String, String> columnMapping) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        ImportHistoryDto history = importExportService.importTransactions(file, format, columnMapping, userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history")
    @Operation(summary = "Get import history", description = "Get import history for the current user")
    public ResponseEntity<List<ImportHistoryDto>> getImportHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        List<ImportHistoryDto> history = importExportService.getImportHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/export/expenses")
    @Operation(summary = "Export expenses", description = "Export expenses to CSV")
    public ResponseEntity<byte[]> exportExpenses(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        byte[] csv = importExportService.exportExpensesCSV(userId, startDate, endDate);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @GetMapping("/export/income")
    @Operation(summary = "Export income", description = "Export income to CSV")
    public ResponseEntity<byte[]> exportIncome(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        byte[] csv = importExportService.exportIncomeCSV(userId, startDate, endDate);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=income.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @GetMapping("/export/backup")
    @Operation(summary = "Export full backup", description = "Export complete data backup")
    public ResponseEntity<byte[]> exportFullBackup() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        byte[] backup = importExportService.exportFullBackup(userId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expensora-backup.json")
            .contentType(MediaType.APPLICATION_JSON)
            .body(backup);
    }
}
