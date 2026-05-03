package com.murdergame.results.controller;

import com.murdergame.results.service.ResultsService;
import com.murdergame.results.dto.ResultsResponse;
import com.murdergame.results.dto.Quiz1DetailResponse;
import com.murdergame.results.dto.Quiz2DetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultsController {

    private final ResultsService resultsService;

    // Takımın tüm sonuçları
    @GetMapping("/team/{teamId}")
    public ResultsResponse getTeamResults(@PathVariable Long teamId) {
        return resultsService.getTeamResults(teamId);
    }

    // Takımın Quiz 1 detayları
    @GetMapping("/quiz1/{teamId}")
    public Quiz1DetailResponse getQuiz1Details(@PathVariable Long teamId) {
        return resultsService.getQuiz1Details(teamId);
    }

    // Takımın Quiz 2 detayları
    @GetMapping("/quiz2/{teamId}")
    public Quiz2DetailResponse getQuiz2Details(@PathVariable Long teamId) {
        return resultsService.getQuiz2Details(teamId);
    }

    // Tüm takımların sonuçları (final ranking)
    @GetMapping("/all")
    public List<ResultsResponse> getAllResults() {
        return resultsService.getAllResults();
    }
}