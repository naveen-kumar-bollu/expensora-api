package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.ImportHistoryDto;
import com.expensora.expensora_api.dto.ImportPreviewDto;
import com.expensora.expensora_api.entity.*;
import com.expensora.expensora_api.repository.*;
import com.expensora.expensora_api.service.ImportExportService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImportExportServiceImpl implements ImportExportService {

    @Autowired
    private ImportHistoryRepository importHistoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public ImportPreviewDto previewImport(MultipartFile file, ImportFormat format) {
        try {
            Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            ImportPreviewDto preview = new ImportPreviewDto();
            preview.setHeaders(new ArrayList<>(csvParser.getHeaderMap().keySet()));
            preview.setTotalRows((int) csvParser.getRecords().size());

            // Get first 5 rows for preview
            List<Map<String, String>> previewData = new ArrayList<>();
            int count = 0;
            for (CSVRecord record : csvParser) {
                if (count >= 5) break;
                Map<String, String> row = new HashMap<>();
                for (String header : preview.getHeaders()) {
                    row.put(header, record.get(header));
                }
                previewData.add(row);
                count++;
            }
            preview.setPreviewData(previewData);

            csvParser.close();
            return preview;
        } catch (IOException e) {
            throw new RuntimeException("Failed to preview file: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ImportHistoryDto importTransactions(MultipartFile file, ImportFormat format, 
                                               Map<String, String> columnMapping, UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        ImportHistory history = new ImportHistory();
        history.setFileName(file.getOriginalFilename());
        history.setFormat(format);
        history.setUser(user);
        history.setStatus(ImportStatus.PROCESSING);
        history = importHistoryRepository.save(history);

        int totalRecords = 0;
        int successfulRecords = 0;
        int failedRecords = 0;
        int duplicateRecords = 0;
        StringBuilder errorLog = new StringBuilder();

        try {
            Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            for (CSVRecord record : csvParser) {
                totalRecords++;
                try {
                    // Basic import logic - can be enhanced
                    String description = record.get(columnMapping.getOrDefault("description", "Description"));
                    BigDecimal amount = new BigDecimal(record.get(columnMapping.getOrDefault("amount", "Amount")));
                    String dateStr = record.get(columnMapping.getOrDefault("date", "Date"));
                    LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);

                    // Create expense
                    Expense expense = new Expense();
                    expense.setDescription(description);
                    expense.setAmount(amount);
                    expense.setExpenseDate(date);
                    expense.setUser(user);
                    expenseRepository.save(expense);

                    successfulRecords++;
                } catch (Exception e) {
                    failedRecords++;
                    errorLog.append("Row ").append(totalRecords).append(": ").append(e.getMessage()).append("\n");
                }
            }

            csvParser.close();

            history.setTotalRecords(totalRecords);
            history.setSuccessfulRecords(successfulRecords);
            history.setFailedRecords(failedRecords);
            history.setDuplicateRecords(duplicateRecords);
            history.setErrorLog(errorLog.toString());
            history.setStatus(ImportStatus.COMPLETED);

        } catch (IOException e) {
            history.setStatus(ImportStatus.FAILED);
            history.setErrorLog("Failed to process file: " + e.getMessage());
        }

        history = importHistoryRepository.save(history);
        return mapToDto(history);
    }

    @Override
    public List<ImportHistoryDto> getImportHistory(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return importHistoryRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    public byte[] exportExpensesCSV(UUID userId, String startDate, String endDate) {
        try {
            StringWriter writer = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                "Date", "Description", "Amount", "Category", "Notes", "Tags"));

            // Fetch and write expenses
            // Implementation simplified for now

            csvPrinter.close();
            return writer.toString().getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export expenses: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportIncomeCSV(UUID userId, String startDate, String endDate) {
        // Similar to exportExpensesCSV
        return new byte[0];
    }

    @Override
    public byte[] exportFullBackup(UUID userId) {
        // Full backup export - simplified for now
        return new byte[0];
    }

    private ImportHistoryDto mapToDto(ImportHistory history) {
        ImportHistoryDto dto = new ImportHistoryDto();
        dto.setId(history.getId());
        dto.setFileName(history.getFileName());
        dto.setFormat(history.getFormat());
        dto.setTotalRecords(history.getTotalRecords());
        dto.setSuccessfulRecords(history.getSuccessfulRecords());
        dto.setFailedRecords(history.getFailedRecords());
        dto.setDuplicateRecords(history.getDuplicateRecords());
        dto.setErrorLog(history.getErrorLog());
        dto.setStatus(history.getStatus());
        dto.setUserId(history.getUser().getId());
        dto.setCreatedAt(history.getCreatedAt());
        dto.setUpdatedAt(history.getUpdatedAt());
        return dto;
    }
}
