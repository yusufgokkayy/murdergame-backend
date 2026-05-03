package com.murdergame.cluegame.dto;

public record FinalAnswerResponse(
        Long id,
        String finalGuess,
        Boolean isCorrect,
        String killerName,
        String trueStory
) {}