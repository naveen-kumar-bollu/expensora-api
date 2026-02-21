package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.ChangePasswordRequestDto;
import com.expensora.expensora_api.dto.RegisterRequestDto;
import com.expensora.expensora_api.dto.UpdateProfileRequestDto;
import com.expensora.expensora_api.entity.Role;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.UserService;
import com.expensora.expensora_api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User register(RegisterRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER); // default role
        return userRepository.save(user);
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User updateProfile(UUID userId, UpdateProfileRequestDto dto) {
        User user = findById(userId);
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        return userRepository.save(user);
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequestDto dto) {
        User user = findById(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public String generateRefreshToken(String email) {
        String refreshToken = jwtUtil.generateRefreshToken(email);
        User user = findByEmail(email);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        return refreshToken;
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        String email = jwtUtil.extractUsername(refreshToken);
        User user = findByEmail(email);
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        if (!jwtUtil.validateToken(refreshToken, email)) {
            throw new RuntimeException("Refresh token expired");
        }
        return jwtUtil.generateToken(email);
    }

    @Override
    public void logout(String email) {
        User user = findByEmail(email);
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}