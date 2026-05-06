package com.murdergame.quiz.dto;

public record SubmitAnswerWSRequest(
        Long questionId,
        String selectedAnswer,  // "A", "B", "C", "D"
        Integer betAmount
) {}