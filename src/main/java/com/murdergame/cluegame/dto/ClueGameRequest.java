package com.murdergame.cluegame.dto;

import java.util.List;

public record ClueGameRequest(
        String killerName,
        String trueStory,
        List<String> clues
) {}