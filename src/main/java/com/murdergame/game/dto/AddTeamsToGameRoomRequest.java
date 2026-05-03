package com.murdergame.game.dto;

import java.util.List;

public record AddTeamsToGameRoomRequest(
        List<Long> teamIds
) {}