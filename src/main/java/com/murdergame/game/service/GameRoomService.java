package com.murdergame.game.service;

import com.murdergame.game.dto.GameRoomResponse;
import com.murdergame.game.entity.GameRoom;
import com.murdergame.game.entity.GameRoomState;

import java.util.List;

public interface GameRoomService {
    GameRoom createGameRoom();
    GameRoom getGameRoom(Long roomId);
    GameRoom updateGameRoomState(Long roomId, GameRoomState newState);
    List<GameRoomResponse> getAllGameRooms();
    void addTeamToGameRoom(Long roomId, Long teamId);
    void removeTeamFromGameRoom(Long roomId, Long teamId);

    // ✅ YENİ
    void addTeamsToGameRoom(Long roomId, List<Long> teamIds);

    void resetGameRoom(Long roomId);
    // YENİ EKLENEN
    void deleteGameRoom(Long roomId);

    void resetGameRoomQuestionsOnly(Long roomId);
}