package com.murdergame.quiz.dto;

public record QuestionResponse(
        Long id,
        String question,
        Integer points
) {}