package com.murdergame.results.service.impl;

import com.murdergame.results.service.ResultsService;
import com.murdergame.results.dto.ResultsResponse;
import com.murdergame.results.dto.Quiz1DetailResponse;
import com.murdergame.results.dto.Quiz2DetailResponse;
import com.murdergame.quiz.repository.QuizAnswerRepository;
import com.murdergame.team.entity.Team;
import com.murdergame.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultsServiceImpl implements ResultsService {

    private final TeamRepository teamRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    @Override
    public ResultsResponse getTeamResults(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Takım bulunamadı"));

        var quiz1Details = getQuiz1Details(teamId);
        var quiz2Details = getQuiz2Details(teamId);

        // Artık quiz2Details null dönmediği için BURASI ÇÖKMEYECEK!
        int finalPoints = quiz1Details.totalPoints() + quiz2Details.netPoints();

        return new ResultsResponse(
                teamId,
                team.getTeamNo(),
                quiz1Details.totalPoints(),
                quiz1Details.totalCorrect(),
                quiz1Details.totalWrong(),
                quiz2Details.netPoints(),
                quiz2Details.correctBets(),
                quiz2Details.wrongBets(),
                finalPoints
        );
    }

    @Override
    public Quiz1DetailResponse getQuiz1Details(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Takım bulunamadı"));

        // Takımın tüm cevaplarını çek, sadece bahissiz (betAmount = 0) olanları al
        var quiz1Answers = quizAnswerRepository.findByTeamId(teamId).stream()
                .filter(a -> a.getBetAmount() == null || a.getBetAmount() == 0)
                .toList();

        int correct = (int) quiz1Answers.stream().filter(a -> a.getIsCorrect()).count();
        int wrong = (int) quiz1Answers.stream().filter(a -> !a.getIsCorrect()).count();
        int totalPoints = quiz1Answers.stream()
                .mapToInt(a -> a.getPointsEarned() != null ? a.getPointsEarned() : 0)
                .sum();

        return new Quiz1DetailResponse(
                teamId,
                team.getTeamNo(),
                correct,
                wrong,
                totalPoints,
                quiz1Answers.size()
        );
    }

    @Override
    public Quiz2DetailResponse getQuiz2Details(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Takım bulunamadı"));

        // Takımın tüm cevaplarını çek, sadece bahisli (betAmount > 0) olanları al
        var bets = quizAnswerRepository.findByTeamId(teamId).stream()
                .filter(a -> a.getBetAmount() != null && a.getBetAmount() > 0)
                .toList();

        int correctBets = (int) bets.stream().filter(a -> a.getIsCorrect()).count();
        int wrongBets = (int) bets.stream().filter(a -> !a.getIsCorrect()).count();

        int totalGained = bets.stream()
                .filter(a -> a.getIsCorrect())
                .mapToInt(a -> a.getPointsEarned() > 0 ? a.getPointsEarned() : 0)
                .sum();

        int totalLost = bets.stream()
                .filter(a -> !a.getIsCorrect())
                .mapToInt(a -> a.getPointsEarned() < 0 ? Math.abs(a.getPointsEarned()) : 0)
                .sum();

        int netPoints = bets.stream()
                .mapToInt(a -> a.getPointsEarned() != null ? a.getPointsEarned() : 0)
                .sum();

        return new Quiz2DetailResponse(
                teamId,
                team.getTeamNo(),
                correctBets,
                wrongBets,
                totalGained,
                totalLost,
                netPoints
        );
    }

    @Override
    public List<ResultsResponse> getAllResults() {
        return teamRepository.findAll()
                .stream()
                .map(team -> getTeamResults(team.getId()))
                .sorted((a, b) -> Integer.compare(b.finalPoints(), a.finalPoints()))
                .collect(Collectors.toList());
    }
}