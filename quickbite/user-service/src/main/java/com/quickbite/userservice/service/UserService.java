package com.quickbite.userservice.service;

import com.quickbite.userservice.dto.*;
import com.quickbite.userservice.entity.User;
import com.quickbite.userservice.exception.ResourceAlreadyExistsException;
import com.quickbite.userservice.exception.ResourceNotFoundException;
import com.quickbite.userservice.repository.UserRepository;
import com.quickbite.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered: " + request.getEmail());
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .build();
        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return buildAuthResponse(user);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        UserResponse r = new UserResponse();
        r.setId(user.getId()); r.setEmail(user.getEmail());
        r.setFullName(user.getFullName()); r.setPhone(user.getPhone());
        r.setRole(user.getRole().name()); r.setCreatedAt(user.getCreatedAt());
        return r;
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(
                String.valueOf(user.getId()),
                Map.of("role", user.getRole().name(), "email", user.getEmail()));
        return AuthResponse.builder()
                .token(token).userId(user.getId())
                .email(user.getEmail()).role(user.getRole().name())
                .build();
    }
}