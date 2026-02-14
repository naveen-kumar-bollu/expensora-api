package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.InsightsDto;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.InsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/insights")
@Tag(name = "Insights", description = "Financial insights and recommendations APIs")
public class InsightsController {

    @Autowired
    private InsightsService insightsService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get financial insights", description = "Get AI-generated financial insights and spending recommendations for a month")
    public ResponseEntity<InsightsDto> getInsights(@RequestParam int month, @RequestParam int year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        InsightsDto insights = insightsService.generateInsights(user.getId(), month, year);
        return ResponseEntity.ok(insights);
    }
}
