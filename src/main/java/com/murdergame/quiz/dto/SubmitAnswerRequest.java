package com.murdergame.quiz.dto;

public record SubmitAnswerRequest(
        Long questionId,
        String answer
) {}