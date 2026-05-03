package com.murdergame.leaderboard.service;

import com.murdergame.leaderboard.dto.LeaderboardResponse;
import java.util.List;

public interface LeaderboardService {
    List<LeaderboardResponse> getLeaderboard();
    LeaderboardResponse getTeamScore(Long teamId);
}