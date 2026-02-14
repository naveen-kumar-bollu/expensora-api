package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.HouseholdCreateRequestDto;
import com.expensora.expensora_api.dto.HouseholdDto;
import com.expensora.expensora_api.entity.HouseholdRole;

import java.util.List;
import java.util.UUID;

public interface HouseholdService {
    HouseholdDto createHousehold(HouseholdCreateRequestDto request, UUID userId);
    HouseholdDto updateHousehold(UUID id, HouseholdCreateRequestDto request, UUID userId);
    void deleteHousehold(UUID id, UUID userId);
    HouseholdDto getHousehold(UUID id, UUID userId);
    List<HouseholdDto> getUserHouseholds(UUID userId);
    void addMember(UUID householdId, String userEmail, HouseholdRole role, UUID ownerId);
    void removeMember(UUID householdId, UUID memberId, UUID ownerId);
}
