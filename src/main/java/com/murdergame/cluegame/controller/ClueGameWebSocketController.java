package com.murdergame.cluegame.controller;

import com.murdergame.cluegame.dto.GuessRequest;
import com.murdergame.cluegame.dto.GuessResponse;
import com.murdergame.cluegame.dto.FinalAnswerRequest;
import com.murdergame.cluegame.dto.FinalAnswerResponse;
import com.murdergame.cluegame.service.ClueGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ClueGameWebSocketController {

    private final ClueGameService clueGameService;
    private final SimpMessagingTemplate messagingTemplate;

    // /app/clue-guess → /topic/clue-game/{{teamId}}/guesses
    @MessageMapping("/clue-guess")
    public void submitGuess(GuessRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (userId == null) return;

        GuessResponse response = clueGameService.submitGuess(userId, request);

        // TÜM TAKIMDA BROADCAST ET
        messagingTemplate.convertAndSend(
                "/topic/clue-game/" + request.teamId() + "/guesses",
                response
        );

        // Admin ekranına broadcast — puan bekliyor
        messagingTemplate.convertAndSend(
                "/topic/clue-game/admin/guesses",
                Map.of(
                        "guessId", response.id(),
                        "teamId", request.teamId(),
                        "guessedName", request.guessedName(),
                        "status", "PENDING"
                )
        );
    }

    // /app/final-answer → /topic/clue-game/{{teamId}}/final
    @MessageMapping("/final-answer")
    public void submitFinalAnswer(FinalAnswerRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (userId == null) return;

        FinalAnswerResponse response = clueGameService.submitFinalAnswer(userId, request);

        // TÜM TAKIMDA BROADCAST ET
        messagingTemplate.convertAndSend(
                "/topic/clue-game/" + request.teamId() + "/final",
                response
        );
    }

    // Admin: 30 dakika bitti, final cevap zamanı
    @MessageMapping("/final-answer-time")
    public void finalAnswerTime(Long clueGameId) {
        messagingTemplate.convertAndSend(
                "/topic/clue-game/final-time",
                clueGameId
        );
    }

}