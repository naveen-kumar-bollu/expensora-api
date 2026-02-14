package com.expensora.expensora_api.service;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public interface ReportService {
    ByteArrayOutputStream exportExpensesToCsv(UUID userId, Integer month, Integer year);
    ByteArrayOutputStream exportIncomeToCsv(UUID userId, Integer month, Integer year);
    ByteArrayOutputStream generateMonthlySummaryReport(UUID userId, Integer month, Integer year);
}
