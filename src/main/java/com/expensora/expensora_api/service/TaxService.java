package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.TaxReportDto;

import java.util.List;
import java.util.UUID;

public interface TaxService {
    List<TaxReportDto> getTaxReport(UUID userId, Integer year);
    List<TaxReportDto> getQuarterlyTaxReport(UUID userId, Integer year, Integer quarter);
}
