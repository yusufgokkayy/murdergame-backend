package com.murdergame.auth.service.impl;

import com.murdergame.auth.entity.Admin;
import com.murdergame.auth.repository.AdminRepository;
import com.murdergame.auth.dto.*;
import com.murdergame.auth.service.AuthService;
import com.murdergame.common.exception.BusinessException;
import com.murdergame.common.exception.InvalidCredentialsException;
import com.murdergame.common.exception.ResourceNotFoundException;
import com.murdergame.common.exception.ValidationException;
import com.murdergame.security.JwtService;
import com.murdergame.team.entity.Team;
import com.murdergame.team.repository.TeamRepository;
import com.murdergame.user.entity.User;
import com.murdergame.user.repository.UserRepository;
import com.murdergame.user.validator.UsernameValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminRepository adminRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsernameValidator usernameValidator;

    @Value("${app.jwt.access-expiration-hours:8}")
    private long accessExpHours;

    @Override
    @Transactional
    public AuthResponse adminLogin(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsernameAndActiveTrue(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Geçersiz kimlik bilgileri"));

        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new InvalidCredentialsException("Geçersiz kimlik bilgileri");
        }

        String subject = "ADMIN:" + admin.getId();
        String accessToken = jwtService.generateAccessToken(subject, Map.of(
                "role", "ADMIN",
                "adminId", admin.getId(),
                "username", admin.getUsername()
        ));

        return new AuthResponse(
                accessToken,
                // refreshToken silinir
                "ADMIN",
                admin.getId(),
                null,
                admin.getUsername()
        );
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getTeam() != null ? user.getTeam().getId() : null
                ))
                .toList();
    }

    @Override
    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Kullanıcı bulunamadı: " + userId));

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public AuthResponse userRegister(UserRegisterRequest request) {
        String validationError = usernameValidator.getValidationError(request.username());
        if (validationError != null) {
            throw new ValidationException(validationError);
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Bu kullanıcı adı zaten alınmış");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setActive(true);
        user.setTeam(null);

        User saved = userRepository.save(user);

        String subject = "USER:" + saved.getId();
        String accessToken = jwtService.generateAccessToken(subject, Map.of(
                "role", "USER",
                "userId", saved.getId(),
                "username", saved.getUsername()
        ));

        return new AuthResponse(
                accessToken,
                // refreshToken silinir
                "USER",
                saved.getId(),
                null,
                saved.getUsername()
        );
    }

    @Override
    @Transactional
    public AuthResponse userLogin(UserLoginRequest request) {
        // Sadece username ile aktif kullanıcıyı bul
        User user = userRepository.findByUsernameAndActiveTrue(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Kullanıcının takımı varsa ID'sini al, yoksa null kalır
        Long teamId = (user.getTeam() != null) ? user.getTeam().getId() : null;

        // Map.of null değer kabul etmediği için HashMap kullanıyoruz
        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", "USER");
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        if (teamId != null) {
            claims.put("teamId", teamId);
        }

        String subject = "USER:" + user.getId();
        String accessToken = jwtService.generateAccessToken(subject, claims);

        return new AuthResponse(
                accessToken,
                "USER",
                user.getId(),
                teamId,  // Takımı yoksa front-end'e null döner, varsa ID'si döner
                user.getUsername()
        );
    }
}