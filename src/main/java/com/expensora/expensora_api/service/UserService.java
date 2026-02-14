package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.ChangePasswordRequestDto;
import com.expensora.expensora_api.dto.RegisterRequestDto;
import com.expensora.expensora_api.dto.UpdateProfileRequestDto;
import com.expensora.expensora_api.entity.User;

import java.util.UUID;

public interface UserService {
    User register(RegisterRequestDto dto);
    User findById(UUID id);
    User findByEmail(String email);
    User updateProfile(UUID userId, UpdateProfileRequestDto dto);
    void changePassword(UUID userId, ChangePasswordRequestDto dto);
    String generateRefreshToken(String email);
    String refreshAccessToken(String refreshToken);
    void logout(String email);
}