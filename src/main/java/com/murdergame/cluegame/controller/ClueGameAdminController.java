package com.murdergame.cluegame.controller;

import com.murdergame.cluegame.entity.ClueGameGuess;
import com.murdergame.cluegame.repository.ClueGameGuessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/clue-game")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ClueGameAdminController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ClueGameGuessRepository clueGameGuessRepository;

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

    @PostMapping("/extend-time/{roomId}")
    public void extendTime(
            @PathVariable Long roomId,
            @RequestBody Map<String, Integer> body) {

        Integer minutes = body.get("minutes");
        if (minutes == null || minutes <= 0) minutes = 5;

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/time-extended",
                Map.of("extraMinutes", minutes)
        );
    }

    @PostMapping("/guess/{guessId}/score")
    public void scoreGuess(
            @PathVariable Long guessId,
            @RequestBody Map<String, Integer> body) {

        Integer score = body.get("score");
        if (score == null || (!score.equals(0) && !score.equals(50) && !score.equals(100))) {
            throw new RuntimeException("Geçersiz puan, 0/50/100 olmalı");
        }

        ClueGameGuess guess = clueGameGuessRepository.findById(guessId)
                .orElseThrow(() -> new RuntimeException("Tahmin bulunamadı"));

        if (guess.getScored()) {
            throw new RuntimeException("Bu tahmin zaten puanlandı");
        }

        guess.setAdminScore(score);
        guess.setPointsEarned(score);
        guess.setScored(true);
        clueGameGuessRepository.save(guess);

        // Takıma broadcast et
        messagingTemplate.convertAndSend(
                "/topic/clue-game/" + guess.getTeam().getId() + "/score",
                Map.of(
                        "guessId", guessId,
                        "score", score,
                        "teamId", guess.getTeam().getId()
                )
        );

        // Admin ekranına da broadcast et
        messagingTemplate.convertAndSend(
                "/topic/clue-game/admin/scored",
                Map.of(
                        "guessId", guessId,
                        "score", score,
                        "teamId", guess.getTeam().getId(),
                        "guessedName", guess.getGuessedName()
                )
        );
    }
}