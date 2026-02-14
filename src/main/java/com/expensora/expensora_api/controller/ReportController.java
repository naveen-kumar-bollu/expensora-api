package com.expensora.expensora_api.controller;

import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.ReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Report generation and export APIs")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/expenses/csv")
    @Operation(summary = "Export expenses to CSV", description = "Download expenses data as CSV file with optional month/year filter")
    public ResponseEntity<byte[]> exportExpenses(@RequestParam(required = false) Integer month,
                                                   @RequestParam(required = false) Integer year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        ByteArrayOutputStream csv = reportService.exportExpensesToCsv(user.getId(), month, year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toByteArray());
    }

    @GetMapping("/income/csv")
    @Operation(summary = "Export income to CSV", description = "Download income data as CSV file with optional month/year filter")
    public ResponseEntity<byte[]> exportIncome(@RequestParam(required = false) Integer month,
                                                 @RequestParam(required = false) Integer year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        ByteArrayOutputStream csv = reportService.exportIncomeToCsv(user.getId(), month, year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=income.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toByteArray());
    }

    @GetMapping("/monthly-summary")
    @Operation(summary = "Generate monthly summary report", description = "Download a text report with detailed monthly financial summary")
    public ResponseEntity<byte[]> getMonthlySummary(@RequestParam Integer month, @RequestParam Integer year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        ByteArrayOutputStream report = reportService.generateMonthlySummaryReport(user.getId(), month, year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=monthly-summary.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(report.toByteArray());
    }
}
