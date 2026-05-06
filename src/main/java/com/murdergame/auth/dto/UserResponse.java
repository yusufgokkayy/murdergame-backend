package com.murdergame.auth.dto;

public record UserResponse (
    Long userId,
    String username,
    Long teamId
) {}
