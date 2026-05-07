package com.murdergame.game.service.impl;

import com.murdergame.common.exception.ValidationException;
import com.murdergame.game.dto.GameRoomResponse;
import com.murdergame.game.entity.GameRoom;
import com.murdergame.game.entity.GameRoomState;
import com.murdergame.game.repository.GameRoomRepository;
import com.murdergame.game.service.GameRoomService;
import com.murdergame.team.entity.Team;
import com.murdergame.team.repository.TeamRepository;
import com.murdergame.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GameRoomServiceImpl implements GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final TeamRepository teamRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public GameRoom createGameRoom() {
        GameRoom gameRoom = GameRoom.builder()
                .state(GameRoomState.WAITING)
                .active(true)
                .build();
        return gameRoomRepository.save(gameRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public GameRoom getGameRoom(Long roomId) {
        return gameRoomRepository.findByIdAndActiveTrue(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı"));
    }

    @Override
    public GameRoom updateGameRoomState(Long roomId, GameRoomState newState) {
        GameRoom gameRoom = getGameRoom(roomId);
        gameRoom.setState(newState);

        // Eğer ENDED durumuna geçiyorsa, endedAt'ı set et
        if (newState == GameRoomState.ENDED) {
            gameRoom.setEndedAt(LocalDateTime.now());
        }
        if (newState == GameRoomState.QUIZ1 || newState == GameRoomState.QUIZ2
                || newState == GameRoomState.CLUEGAME) {
            gameRoom.setStartedAt(LocalDateTime.now());
        }

        GameRoom saved = gameRoomRepository.save(gameRoom);

        // ✅ YENİ — state değişimini broadcast et
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/state",
                saved.getState().name()   // "QUIZ1", "QUIZ2" vs string olarak gider
        );

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameRoomResponse> getAllGameRooms() {
        return gameRoomRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void addTeamToGameRoom(Long roomId, Long teamId) {
        GameRoom gameRoom = getGameRoom(roomId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Takım bulunamadı"));

        team.setGameRoom(gameRoom);
        teamRepository.save(team);
    }

    @Override
    public void removeTeamFromGameRoom(Long roomId, Long teamId) {
        GameRoom gameRoom = getGameRoom(roomId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Takım bulunamadı"));

        if (!team.getGameRoom().getId().equals(roomId)) {
            throw new ResourceNotFoundException("Takım bu odada değil");
        }

        team.setGameRoom(null);
        teamRepository.save(team);
    }

    @Override
    public void addTeamsToGameRoom(Long roomId, List<Long> teamIds) {
        GameRoom gameRoom = getGameRoom(roomId);

        if (teamIds == null || teamIds.isEmpty()) {
            throw new ValidationException("Takım listesi boş olamaz");
        }

        List<Team> teams = teamRepository.findAllById(teamIds);

        if (teams.size() != teamIds.size()) {
            throw new ResourceNotFoundException("Bazı takımlar bulunamadı");
        }

        // Tüm takımları odaya ekle
        teams.forEach(team -> team.setGameRoom(gameRoom));
        teamRepository.saveAll(teams);
    }

    // ...
    @Override
    public void deleteGameRoom(Long roomId) {
        GameRoom gameRoom = getGameRoom(roomId); // Bu metod zaten sadece aktif odaları bulur

        // 1. Önce takımları bu odadan çıkaralım (Takımlar tamamen silinmesin diye)
        List<Team> teams = gameRoom.getTeams();
        if (teams != null && !teams.isEmpty()) {
            teams.forEach(team -> team.setGameRoom(null));
            teamRepository.saveAll(teams);
        }

        // 2. Odayı pasife çek (Soft Delete)
        // Böylece geçmiş veriler patlamaz ama oda listesinde (all) bir daha görünmez.
        gameRoom.setActive(false);
        gameRoomRepository.save(gameRoom);
    }
// ...

    private GameRoomResponse toResponse(GameRoom gameRoom) {
        return new GameRoomResponse(
                gameRoom.getId(),
                gameRoom.getState(),
                gameRoom.getTeams().size(),
                gameRoom.getCreatedAt(),
                gameRoom.getStartedAt(),
                gameRoom.getEndedAt()
        );
    }
}