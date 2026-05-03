package com.murdergame.cluegame.dto;

public record GuessResponse(
        Long id,
        String guessedName,
        Boolean isCorrect,
        Integer pointsEarned,
        Long newTeamTotal
) {}