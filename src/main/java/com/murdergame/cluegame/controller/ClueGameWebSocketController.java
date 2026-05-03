package com.murdergame.cluegame.controller;

import com.murdergame.cluegame.dto.GuessRequest;
import com.murdergame.cluegame.dto.GuessResponse;
import com.murdergame.cluegame.dto.FinalAnswerRequest;
import com.murdergame.cluegame.dto.FinalAnswerResponse;
import com.murdergame.cluegame.service.ClueGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ClueGameWebSocketController {

    private final ClueGameService clueGameService;
    private final SimpMessagingTemplate messagingTemplate;

    // /app/clue-guess → /topic/clue-game/{{teamId}}/guesses
    @MessageMapping("/clue-guess")
    public void submitGuess(GuessRequest request) {
        Long userId = 1L; // ⚠️ WebSocket'ten userId al (JWT ile)

        GuessResponse response = clueGameService.submitGuess(userId, request);

        // TÜM TAKIMDA BROADCAST ET
        messagingTemplate.convertAndSend(
                "/topic/clue-game/" + request.teamId() + "/guesses",
                response
        );
    }

    // /app/final-answer → /topic/clue-game/{{teamId}}/final
    @MessageMapping("/final-answer")
    public void submitFinalAnswer(FinalAnswerRequest request) {
        Long userId = 1L; // ⚠️ WebSocket'ten userId al (JWT ile)

        FinalAnswerResponse response = clueGameService.submitFinalAnswer(userId, request);

        // TÜM TAKIMDA BROADCAST ET
        messagingTemplate.convertAndSend(
                "/topic/clue-game/" + request.teamId() + "/final",
                response
        );
    }

    // Admin: Oyunu başlat (ipuçlarını gönder)
    @MessageMapping("/start-clue-game")
    public void startClueGame(Long clueGameId) {
        messagingTemplate.convertAndSend(
                "/topic/clue-game/start",
                clueGameId
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

    // Admin: Oyun bitti, sonuçlar göster
    @MessageMapping("/end-clue-game")
    public void endClueGame(Long clueGameId) {
        messagingTemplate.convertAndSend(
                "/topic/clue-game/end",
                clueGameId
        );
    }
}