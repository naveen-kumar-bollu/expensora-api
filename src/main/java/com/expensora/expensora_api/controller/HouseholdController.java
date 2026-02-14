package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.HouseholdCreateRequestDto;
import com.expensora.expensora_api.dto.HouseholdDto;
import com.expensora.expensora_api.entity.HouseholdRole;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.HouseholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/households")
@Tag(name = "Households", description = "Shared finances and family account APIs")
public class HouseholdController {

    @Autowired
    private HouseholdService householdService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a household", description = "Create a new shared household account")
    public ResponseEntity<HouseholdDto> create(@RequestBody HouseholdCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        HouseholdDto household = householdService.createHousehold(dto, userId);
        return ResponseEntity.ok(household);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update household", description = "Update household information")
    public ResponseEntity<HouseholdDto> update(@PathVariable UUID id, @RequestBody HouseholdCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        HouseholdDto household = householdService.updateHousehold(id, dto, userId);
        return ResponseEntity.ok(household);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete household", description = "Delete a household")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        householdService.deleteHousehold(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get household", description = "Get household details")
    public ResponseEntity<HouseholdDto> getHousehold(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        HouseholdDto household = householdService.getHousehold(id, userId);
        return ResponseEntity.ok(household);
    }

    @GetMapping
    @Operation(summary = "Get user households", description = "Get all households for the current user")
    public ResponseEntity<List<HouseholdDto>> getUserHouseholds() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        List<HouseholdDto> households = householdService.getUserHouseholds(userId);
        return ResponseEntity.ok(households);
    }

    @PostMapping("/{householdId}/members")
    @Operation(summary = "Add member", description = "Add a member to the household")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID householdId,
            @RequestBody Map<String, String> request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        String userEmail = request.get("email");
        HouseholdRole role = HouseholdRole.valueOf(request.get("role"));

        householdService.addMember(householdId, userEmail, role, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{householdId}/members/{memberId}")
    @Operation(summary = "Remove member", description = "Remove a member from the household")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID householdId,
            @PathVariable UUID memberId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        householdService.removeMember(householdId, memberId, userId);
        return ResponseEntity.noContent().build();
    }
}
