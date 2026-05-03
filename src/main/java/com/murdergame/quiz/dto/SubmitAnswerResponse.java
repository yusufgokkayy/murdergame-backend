package com.murdergame.quiz.dto;

public record SubmitAnswerResponse(
        Long answerId,
        Long questionId,
        Boolean isCorrect,
        Integer pointsEarned,
        String message
) {}