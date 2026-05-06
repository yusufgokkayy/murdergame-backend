package com.murdergame.cluegame.dto;

public record GuessResponse(
        Long id,
        String guessedName,
        Boolean isCorrect,      // null = admin henüz değerlendirmedi
        Integer pointsEarned,
        Long newTeamTotal       // null = henüz hesaplanmadı
) {}