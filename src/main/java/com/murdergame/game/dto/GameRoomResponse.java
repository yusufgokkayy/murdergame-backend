package com.murdergame.game.dto;

import com.murdergame.game.entity.GameRoomState;
import java.time.LocalDateTime;

public record GameRoomResponse(
        Long id,
        GameRoomState state,
        Integer teamCount,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {}