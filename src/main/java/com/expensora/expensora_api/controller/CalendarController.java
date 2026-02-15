package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.CalendarEventDto;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/calendar")
@Tag(name = "Calendar", description = "Calendar view APIs")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/events")
    @Operation(summary = "Get calendar events", description = "Get all transactions for calendar view within a date range")
    public ResponseEntity<List<CalendarEventDto>> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<CalendarEventDto> events = calendarService.getCalendarEvents(user.getId(), startDate, endDate);
        return ResponseEntity.ok(events);
    }
}
