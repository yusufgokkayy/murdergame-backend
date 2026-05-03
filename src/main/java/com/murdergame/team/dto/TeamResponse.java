package com.murdergame.team.dto;

public record TeamResponse(
        Long id,
        String teamNo,
        Boolean active
) {}