package com.murdergame.quiz.service;

import com.murdergame.quiz.dto.CreateQuestionRequest;
import com.murdergame.quiz.dto.QuestionResponse;
import com.murdergame.quiz.dto.SubmitAnswerRequest;
import com.murdergame.quiz.dto.SubmitAnswerResponse;
import com.murdergame.quiz.entity.QuizAnswer;

import java.util.List;

public interface QuizService {
    // Question'ları GameRoom'a göre getir
    List<QuestionResponse> getQuestionsByGameRoom(Long gameRoomId);

    // Specific question'ı getir
    QuestionResponse getQuestionById(Long questionId);

    // Question oluştur (Admin)
    QuestionResponse createQuestion(Long gameRoomId, CreateQuestionRequest request);

    // Takımın cevabını kaydet
    SubmitAnswerResponse submitAnswer(Long gameRoomId, Long teamId, SubmitAnswerRequest request);

    // GameRoom'daki tüm cevapları getir (leaderboard için)
    List<QuizAnswer> getGameRoomAnswers(Long gameRoomId);

    // Takımın cevaplarını getir
    List<QuizAnswer> getTeamAnswers(Long gameRoomId, Long teamId);
}