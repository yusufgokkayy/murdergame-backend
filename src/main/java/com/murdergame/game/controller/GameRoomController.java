package com.murdergame.game.controller;

import com.murdergame.game.dto.AddTeamsToGameRoomRequest;
import com.murdergame.game.dto.GameRoomResponse;
import com.murdergame.game.dto.UpdateGameRoomStateRequest;
import com.murdergame.game.entity.GameRoom;
import com.murdergame.game.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game-room")
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public GameRoom createGameRoom() {
        return gameRoomService.createGameRoom();
    }

    @GetMapping("/{roomId}")
    public GameRoom getGameRoom(@PathVariable Long roomId) {
        return gameRoomService.getGameRoom(roomId);
    }

    @GetMapping("/admin/all")
    public List<GameRoomResponse> getAllGameRooms() {
        return gameRoomService.getAllGameRooms();
    }

    @PutMapping("/{roomId}/state")
    @PreAuthorize("hasRole('ADMIN')")
    public GameRoom updateGameRoomState(
            @PathVariable Long roomId,
            @RequestBody UpdateGameRoomStateRequest request) {
        return gameRoomService.updateGameRoomState(roomId, request.state());
    }

    // ❌ BU KALDIR (tek tek ekleme)
    // @PostMapping("/{roomId}/add-team/{teamId}")
    // public void addTeamToGameRoom(@PathVariable Long roomId, @PathVariable Long teamId) {
    //     gameRoomService.addTeamToGameRoom(roomId, teamId);
    // }

    // ✅ BULK EKLE (tüm takımları birden)
    @PostMapping("/{roomId}/add-teams")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void addTeamsToGameRoom(
            @PathVariable Long roomId,
            @RequestBody AddTeamsToGameRoomRequest request) {
        gameRoomService.addTeamsToGameRoom(roomId, request.teamIds());
    }

    @PostMapping("/{roomId}/remove-team/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeTeamFromGameRoom(@PathVariable Long roomId, @PathVariable Long teamId) {
        gameRoomService.removeTeamFromGameRoom(roomId, teamId);
    }

    @PostMapping("/{roomId}/reset")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void resetGameRoom(@PathVariable Long roomId) {
        gameRoomService.resetGameRoom(roomId);
    }

    // YENİ EKLENEN ENDPOINT
    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void deleteGameRoom(@PathVariable Long roomId) {
        gameRoomService.deleteGameRoom(roomId);
    }
}