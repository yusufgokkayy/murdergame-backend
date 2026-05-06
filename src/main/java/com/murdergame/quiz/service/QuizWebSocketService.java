package com.murdergame.quiz.service;

import com.murdergame.quiz.dto.SubmitAnswerWSRequest;

public interface QuizWebSocketService {

    void broadcastNextQuestion(Long roomId, Long adminId);

    void submitAnswer(Long roomId, Long userId, SubmitAnswerWSRequest request);

    void endCurrentQuestion(Long roomId);
}