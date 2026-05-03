package com.murdergame.cluegame.dto;

public record GuessRequest(
        Long clueGameId,
        Long teamId,
        String guessedName
) {}