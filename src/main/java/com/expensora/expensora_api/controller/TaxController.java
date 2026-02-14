package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.TaxReportDto;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.TaxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tax")
@Tag(name = "Tax", description = "Tax planning and reporting APIs")
public class TaxController {

    @Autowired
    private TaxService taxService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/report/{year}")
    @Operation(summary = "Get tax report", description = "Get annual tax deductible expenses report")
    public ResponseEntity<List<TaxReportDto>> getTaxReport(@PathVariable Integer year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        List<TaxReportDto> report = taxService.getTaxReport(userId, year);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/report/{year}/quarter/{quarter}")
    @Operation(summary = "Get quarterly tax report", description = "Get quarterly tax deductible expenses report")
    public ResponseEntity<List<TaxReportDto>> getQuarterlyTaxReport(
            @PathVariable Integer year,
            @PathVariable Integer quarter) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        List<TaxReportDto> report = taxService.getQuarterlyTaxReport(userId, year, quarter);
        return ResponseEntity.ok(report);
    }
}
