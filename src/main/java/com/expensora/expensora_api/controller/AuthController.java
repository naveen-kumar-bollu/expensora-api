package com.expensora.expensora_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensora.expensora_api.dto.AuthResponseDto;
import com.expensora.expensora_api.dto.ChangePasswordRequestDto;
import com.expensora.expensora_api.dto.LoginRequestDto;
import com.expensora.expensora_api.dto.RefreshTokenRequestDto;
import com.expensora.expensora_api.dto.RegisterRequestDto;
import com.expensora.expensora_api.dto.UpdateProfileRequestDto;
import com.expensora.expensora_api.dto.UserDto;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.mapper.UserMapper;
import com.expensora.expensora_api.service.UserService;
import com.expensora.expensora_api.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account and return authentication tokens")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto dto) {
        try {
            User user = userService.register(dto);
            String token = jwtUtil.generateToken(user.getEmail());
            String refreshToken = userService.generateRefreshToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponseDto(token, refreshToken));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
            String token = jwtUtil.generateToken(authentication.getName());
            String refreshToken = userService.generateRefreshToken(authentication.getName());
            return ResponseEntity.ok(new AuthResponseDto(token, refreshToken));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate a new access token using a refresh token")
    public ResponseEntity<AuthResponseDto> refreshToken(@RequestBody RefreshTokenRequestDto dto) {
        String newAccessToken = userService.refreshAccessToken(dto.getRefreshToken());
        return ResponseEntity.ok(new AuthResponseDto(newAccessToken, dto.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate user's refresh token")
    public ResponseEntity<Void> logout() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.logout(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change the password for the authenticated user")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email);
        userService.changePassword(user.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update name and other profile information")
    public ResponseEntity<UserDto> updateProfile(@RequestBody UpdateProfileRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email);
        User updated = userService.updateProfile(user.getId(), dto);
        return ResponseEntity.ok(userMapper.toDto(updated));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get the authenticated user's profile information")
    public ResponseEntity<UserDto> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email);
        return ResponseEntity.ok(userMapper.toDto(user));
    }
}