package com.murdergame.cluegame.service.impl;

import com.murdergame.cluegame.service.ClueGameService;
import com.murdergame.cluegame.dto.*;
import com.murdergame.cluegame.entity.ClueGame;
import com.murdergame.cluegame.entity.ClueGameGuess;
import com.murdergame.cluegame.entity.ClueGameFinalAnswer;
import com.murdergame.cluegame.repository.ClueGameRepository;
import com.murdergame.cluegame.repository.ClueGameGuessRepository;
import com.murdergame.cluegame.repository.ClueGameFinalAnswerRepository;
import com.murdergame.quiz.repository.QuizAnswerRepository;
import com.murdergame.team.entity.Team;
import com.murdergame.team.repository.TeamRepository;
import com.murdergame.user.entity.User;
import com.murdergame.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClueGameServiceImpl implements ClueGameService {

    private final ClueGameRepository clueGameRepository;
    private final ClueGameGuessRepository clueGameGuessRepository;
    private final ClueGameFinalAnswerRepository clueGameFinalAnswerRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    @Override
    public ClueGame createClueGame(ClueGameRequest request) {
        ClueGame clueGame = ClueGame.builder()
                .killerName(request.killerName())
                .trueStory(request.trueStory())
                .clues(request.clues())
                .isActive(true)
                .build();

        return clueGameRepository.save(clueGame);
    }

    @Override
    @Transactional(readOnly = true)
    public ClueGameResponse getClueGame(Long clueGameId) {
        ClueGame clueGame = clueGameRepository.findByIdAndIsActiveTrue(clueGameId)
                .orElseThrow(() -> new RuntimeException("Oyun bulunamadı"));

        return new ClueGameResponse(
                clueGame.getId(),
                clueGame.getClues(),
                clueGame.getClues().size()
        );
    }

    @Override
    public GuessResponse submitGuess(Long userId, GuessRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new RuntimeException("Takım bulunamadı"));

        ClueGame clueGame = clueGameRepository.findByIdAndIsActiveTrue(request.clueGameId())
                .orElseThrow(() -> new RuntimeException("Oyun bulunamadı"));

        // Guess kaydet — puan henüz 0, admin sonra verecek
        ClueGameGuess guess = ClueGameGuess.builder()
                .clueGame(clueGame)
                .team(team)
                .user(user)
                .guessedName(request.guessedName())
                .isCorrect(false)   // admin değerlendirecek
                .pointsEarned(0)    // admin verecek
                .scored(false)
                .build();

        clueGameGuessRepository.save(guess);

        return new GuessResponse(
                guess.getId(),
                request.guessedName(),
                null,   // isCorrect admin bilecek
                0,      // puan henüz yok
                null    // total da henüz değişmedi
        );
    }

    @Override
    public FinalAnswerResponse submitFinalAnswer(Long userId, FinalAnswerRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new RuntimeException("Takım bulunamadı"));

        ClueGame clueGame = clueGameRepository.findByIdAndIsActiveTrue(request.clueGameId())
                .orElseThrow(() -> new RuntimeException("Oyun bulunamadı"));

        // Final cevap doğru mu?
        boolean isCorrect = clueGame.getKillerName()
                .equalsIgnoreCase(request.finalGuess().trim());

        // Final answer kaydet
        ClueGameFinalAnswer finalAnswer = ClueGameFinalAnswer.builder()
                .clueGame(clueGame)
                .team(team)
                .finalGuess(request.finalGuess())
                .isCorrect(isCorrect)
                .build();

        clueGameFinalAnswerRepository.save(finalAnswer);

        return new FinalAnswerResponse(
                finalAnswer.getId(),
                request.finalGuess(),
                isCorrect,
                clueGame.getKillerName(),
                isCorrect ? clueGame.getTrueStory() : null
        );
    }
}