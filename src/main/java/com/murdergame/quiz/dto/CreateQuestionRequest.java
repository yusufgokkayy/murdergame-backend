package com.murdergame.quiz.dto;

public record CreateQuestionRequest(
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctAnswer, // A, B, C, D
        Integer points
) {}