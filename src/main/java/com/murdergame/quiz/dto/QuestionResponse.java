package com.murdergame.quiz.dto;

public record QuestionResponse(
        Long id,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String question,
        Integer points
) {}