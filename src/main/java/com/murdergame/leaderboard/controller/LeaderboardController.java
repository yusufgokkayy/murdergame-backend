package com.murdergame.leaderboard.controller;

import com.murdergame.leaderboard.service.LeaderboardService;
import com.murdergame.leaderboard.dto.LeaderboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    // TÜM TAKIMLARIN PUANLARINI GÖSTER
    @GetMapping
    public List<LeaderboardResponse> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }

    // BELİRLİ BİR TAKIMIN PUANINI GÖSTER
    @GetMapping("/{teamId}")
    public LeaderboardResponse getTeamScore(@PathVariable Long teamId) {
        return leaderboardService.getTeamScore(teamId);
    }
}