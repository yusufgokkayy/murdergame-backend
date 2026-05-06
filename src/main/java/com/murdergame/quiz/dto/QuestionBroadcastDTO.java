package com.murdergame.quiz.dto;

public record QuestionBroadcastDTO(
        Long questionId,
        Integer questionIndex,
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        Integer points,
        Integer durationSeconds  // frontend timer için
) {}