package com.murdergame.quiz.controller;

import com.murdergame.quiz.dto.SubmitAnswerWSRequest;
import com.murdergame.quiz.service.QuizWebSocketService;
import com.murdergame.quiz.service.impl.QuizWebSocketServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class QuizWebSocketController {

    private final QuizWebSocketService quizWebSocketService;

    // Admin sonraki soruya geçer
    @MessageMapping("/quiz/{roomId}/next-question")
    public void nextQuestion(
            @DestinationVariable Long roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        // ✅ YENİ — role kontrolü
        System.out.println("=== nextQuestion CALLED roomId: " + roomId + " ===");
        String role = (String) headerAccessor.getSessionAttributes().get("role");
        System.out.println("role: " + role);
        if (!"ADMIN".equals(role)) {
            // Sessizce dön, hata fırlatma — frontend'i bozar
            return;
        }
        System.out.println("=== nextQuestion CALLED roomId: " + roomId + " ===");

        Long adminId = (Long) headerAccessor.getSessionAttributes().get("userId");
        quizWebSocketService.broadcastNextQuestion(roomId, adminId);
    }

    // Admin soruyu bitirir (süre dolunca veya manuel)
    @MessageMapping("/quiz/{roomId}/end-question")
    public void endQuestion(
            @DestinationVariable Long roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        // ✅ YENİ — role kontrolü
        String role = (String) headerAccessor.getSessionAttributes().get("role");
        if (!"ADMIN".equals(role)) {
            return;
        }

        quizWebSocketService.endCurrentQuestion(roomId);
    }

    // Kullanıcı cevap gönderir
    @MessageMapping("/quiz/{roomId}/answer")
    public void submitAnswer(
            @DestinationVariable Long roomId,
            @Payload SubmitAnswerWSRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        quizWebSocketService.submitAnswer(roomId, userId, request);
    }
}

