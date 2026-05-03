package com.murdergame.team.dto;

public record JoinTeamRequest(
        String teamNo,
        String teamPassword,
        String username  // ✅ Ekle
) {}