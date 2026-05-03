package com.murdergame.results.service;

import com.murdergame.results.dto.ResultsResponse;
import com.murdergame.results.dto.Quiz1DetailResponse;
import com.murdergame.results.dto.Quiz2DetailResponse;
import java.util.List;

public interface ResultsService {
    ResultsResponse getTeamResults(Long teamId);
    Quiz1DetailResponse getQuiz1Details(Long teamId);
    Quiz2DetailResponse getQuiz2Details(Long teamId);
    List<ResultsResponse> getAllResults();
}