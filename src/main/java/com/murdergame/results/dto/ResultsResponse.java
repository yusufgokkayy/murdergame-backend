package com.murdergame.results.dto;

import java.util.List;

public record ResultsResponse(
        Long teamId,
        String teamName,
        Integer quiz1Points,
        Integer quiz1Correct,
        Integer quiz1Wrong,
        Integer quiz2Points,
        Integer quiz2CorrectBets,
        Integer quiz2WrongBets,
        Integer finalPoints
) {}