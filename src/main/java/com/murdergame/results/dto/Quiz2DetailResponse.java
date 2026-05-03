package com.murdergame.results.dto;

public record Quiz2DetailResponse(
        Long teamId,
        String teamName,
        Integer correctBets,
        Integer wrongBets,
        Integer totalGained,
        Integer totalLost,
        Integer netPoints
) {}