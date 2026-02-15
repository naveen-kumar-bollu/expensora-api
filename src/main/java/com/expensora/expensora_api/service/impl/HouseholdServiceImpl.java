package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.HouseholdCreateRequestDto;
import com.expensora.expensora_api.dto.HouseholdDto;
import com.expensora.expensora_api.entity.Household;
import com.expensora.expensora_api.entity.HouseholdRole;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.entity.UserHousehold;
import com.expensora.expensora_api.repository.HouseholdRepository;
import com.expensora.expensora_api.repository.UserHouseholdRepository;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.HouseholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HouseholdServiceImpl implements HouseholdService {

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private UserHouseholdRepository userHouseholdRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public HouseholdDto createHousehold(HouseholdCreateRequestDto request, UUID userId) {
        User owner = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Household household = new Household();
        household.setName(request.getName());
        household.setDescription(request.getDescription());
        household.setOwner(owner);
        household.setIsActive(true);

        household = householdRepository.save(household);

        // Add owner as admin member
        UserHousehold userHousehold = new UserHousehold();
        userHousehold.setUser(owner);
        userHousehold.setHousehold(household);
        userHousehold.setRole(HouseholdRole.ADMIN);
        userHousehold.setIsActive(true);
        userHouseholdRepository.save(userHousehold);

        return mapToDto(household);
    }

    @Override
    public HouseholdDto updateHousehold(UUID id, HouseholdCreateRequestDto request, UUID userId) {
        Household household = householdRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Household not found"));

        if (!household.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        household.setName(request.getName());
        household.setDescription(request.getDescription());

        household = householdRepository.save(household);
        return mapToDto(household);
    }

    @Override
    public void deleteHousehold(UUID id, UUID userId) {
        Household household = householdRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Household not found"));

        if (!household.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Delete all user-household associations first
        List<UserHousehold> userHouseholds = userHouseholdRepository.findByHousehold(household);
        userHouseholdRepository.deleteAll(userHouseholds);

        householdRepository.delete(household);
    }

    @Override
    public HouseholdDto getHousehold(UUID id, UUID userId) {
        Household household = householdRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Household not found"));

        // Check if user is a member
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        userHouseholdRepository.findByUserAndHousehold(user, household)
            .orElseThrow(() -> new RuntimeException("Not a member of this household"));

        return mapToDto(household);
    }

    @Override
    public List<HouseholdDto> getUserHouseholds(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserHousehold> userHouseholds = userHouseholdRepository.findByUserAndIsActiveTrue(user);

        return userHouseholds.stream()
            .map(uh -> mapToDto(uh.getHousehold()))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addMember(UUID householdId, String userEmail, HouseholdRole role, UUID ownerId) {
        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new RuntimeException("Household not found"));

        if (!household.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized");
        }

        User newMember = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Check if already a member
        if (userHouseholdRepository.findByUserAndHousehold(newMember, household).isPresent()) {
            throw new RuntimeException("User is already a member");
        }

        UserHousehold userHousehold = new UserHousehold();
        userHousehold.setUser(newMember);
        userHousehold.setHousehold(household);
        userHousehold.setRole(role);
        userHousehold.setIsActive(true);
        userHouseholdRepository.save(userHousehold);
    }

    @Override
    @Transactional
    public void removeMember(UUID householdId, UUID memberId, UUID ownerId) {
        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new RuntimeException("Household not found"));

        if (!household.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized");
        }

        User member = userRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        UserHousehold userHousehold = userHouseholdRepository.findByUserAndHousehold(member, household)
            .orElseThrow(() -> new RuntimeException("User is not a member"));

        userHouseholdRepository.delete(userHousehold);
    }

    private HouseholdDto mapToDto(Household household) {
        HouseholdDto dto = new HouseholdDto();
        dto.setId(household.getId());
        dto.setName(household.getName());
        dto.setDescription(household.getDescription());
        dto.setOwnerId(household.getOwner().getId());
        dto.setOwnerName(household.getOwner().getName());
        dto.setIsActive(household.getIsActive());
        dto.setCreatedAt(household.getCreatedAt());
        dto.setUpdatedAt(household.getUpdatedAt());
        return dto;
    }
}
