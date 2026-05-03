package com.murdergame.results.dto;

public record Quiz1DetailResponse(
        Long teamId,
        String teamName,
        Integer totalCorrect,
        Integer totalWrong,
        Integer totalPoints,
        Integer totalQuestions
) {}