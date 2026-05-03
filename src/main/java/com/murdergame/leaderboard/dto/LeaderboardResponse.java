package com.murdergame.leaderboard.dto;

public record LeaderboardResponse(
        Long teamId,
        String teamName,
        Integer totalScore
) {}