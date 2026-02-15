package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.CalendarEventDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CalendarService {
    List<CalendarEventDto> getCalendarEvents(UUID userId, LocalDate startDate, LocalDate endDate);
}
