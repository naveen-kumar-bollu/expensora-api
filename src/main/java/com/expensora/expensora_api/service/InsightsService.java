package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.InsightsDto;

import java.util.UUID;

public interface InsightsService {
    InsightsDto generateInsights(UUID userId, int month, int year);
}
