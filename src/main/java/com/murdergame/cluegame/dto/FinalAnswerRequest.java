package com.murdergame.cluegame.dto;

public record FinalAnswerRequest(
        Long clueGameId,
        Long teamId,
        String finalGuess
) {}