package com.murdergame.cluegame.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/clue-game")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ClueGameAdminController {

    private final SimpMessagingTemplate messagingTemplate;

    // Oyunu başlat
    @PostMapping("/start/{clueGameId}")
    @ResponseStatus(HttpStatus.OK)
    public void startClueGame(@PathVariable Long clueGameId) {
        messagingTemplate.convertAndSend("/topic/clue-game/start", clueGameId);
    }

    // 30 dakika sonra: Final cevap zamanı
    @PostMapping("/final-time/{clueGameId}")
    @ResponseStatus(HttpStatus.OK)
    public void finalAnswerTime(@PathVariable Long clueGameId) {
        messagingTemplate.convertAndSend("/topic/clue-game/final-time", clueGameId);
    }

    // Oyun bitti
    @PostMapping("/end/{clueGameId}")
    @ResponseStatus(HttpStatus.OK)
    public void endClueGame(@PathVariable Long clueGameId) {
        messagingTemplate.convertAndSend("/topic/clue-game/end", clueGameId);
    }
}