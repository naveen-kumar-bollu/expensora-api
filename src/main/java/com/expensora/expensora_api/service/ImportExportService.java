package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.ImportHistoryDto;
import com.expensora.expensora_api.dto.ImportPreviewDto;
import com.expensora.expensora_api.entity.ImportFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ImportExportService {
    ImportPreviewDto previewImport(MultipartFile file, ImportFormat format);
    ImportHistoryDto importTransactions(MultipartFile file, ImportFormat format, Map<String, String> columnMapping, UUID userId);
    List<ImportHistoryDto> getImportHistory(UUID userId);
    byte[] exportExpensesCSV(UUID userId, String startDate, String endDate);
    byte[] exportIncomeCSV(UUID userId, String startDate, String endDate);
    byte[] exportFullBackup(UUID userId);
}
