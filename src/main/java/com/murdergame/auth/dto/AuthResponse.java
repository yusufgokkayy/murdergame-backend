package com.murdergame.auth.dto;

public record AuthResponse(
        String accessToken,
        // String refreshToken, ← SİL
        String role,
        Long userId,
        Long teamId,
        String username
) {}