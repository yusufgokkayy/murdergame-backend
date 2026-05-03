package com.murdergame.team.dto;

public record AddUserResponse(
        Long userId,
        String username,
        Long teamId,
        String teamNo,
        String message
) {}