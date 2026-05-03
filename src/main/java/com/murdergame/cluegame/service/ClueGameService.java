package com.murdergame.cluegame.service;

import com.murdergame.cluegame.dto.*;
import com.murdergame.cluegame.entity.ClueGame;

public interface ClueGameService {
    ClueGame createClueGame(ClueGameRequest request);
    ClueGameResponse getClueGame(Long clueGameId);
    GuessResponse submitGuess(Long userId, GuessRequest request);
    FinalAnswerResponse submitFinalAnswer(Long userId, FinalAnswerRequest request);
}