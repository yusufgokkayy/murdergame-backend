package com.murdergame.cluegame.dto;

import java.util.List;

public record ClueGameResponse(
        Long id,
        List<String> clues,
        Integer totalClues
) {}