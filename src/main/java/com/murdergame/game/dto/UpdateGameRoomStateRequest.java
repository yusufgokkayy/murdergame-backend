package com.murdergame.game.dto;

import com.murdergame.game.entity.GameRoomState;

public record UpdateGameRoomStateRequest(
        GameRoomState state
) {}