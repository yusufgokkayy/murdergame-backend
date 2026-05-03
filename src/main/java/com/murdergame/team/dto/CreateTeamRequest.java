package com.murdergame.team.dto;

public record CreateTeamRequest(
        String teamNo,
        String teamPassword
) {}