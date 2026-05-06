package com.murdergame.leaderboard.service.impl;

import com.murdergame.cluegame.repository.ClueGameGuessRepository;
import com.murdergame.leaderboard.service.LeaderboardService;
import com.murdergame.leaderboard.dto.LeaderboardResponse;
import com.murdergame.quiz.repository.QuizAnswerRepository;
import com.murdergame.team.repository.TeamRepository;
import com.murdergame.team.entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service  // ← BUNU EKLE!
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardServiceImpl implements LeaderboardService {

    private final TeamRepository teamRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final ClueGameGuessRepository clueGameGuessRepository;

    @Override
    public List<LeaderboardResponse> getLeaderboard() {
        return teamRepository.findAll()
                .stream()
                .map(this::getTeamScoreFromTeam)
                .sorted((a, b) -> Integer.compare(b.totalScore(), a.totalScore()))
                .collect(Collectors.toList());
    }

    @Override
    public LeaderboardResponse getTeamScore(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Takım bulunamadı: " + teamId));
        return getTeamScoreFromTeam(team);
    }

    private LeaderboardResponse getTeamScoreFromTeam(Team team) {
        int quizScore = quizAnswerRepository.findByTeamId(team.getId())
                .stream()
                .mapToInt(a -> a.getPointsEarned() != null ? a.getPointsEarned() : 0)
                .sum();

        int clueScore = clueGameGuessRepository.findByTeamId(team.getId())
                .stream()
                .mapToInt(g -> g.getPointsEarned() != null ? g.getPointsEarned() : 0)
                .sum();

        return new LeaderboardResponse(
                team.getId(),
                team.getTeamNo(),
                quizScore + clueScore
        );
    }
}