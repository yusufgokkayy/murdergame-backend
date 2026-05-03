package com.murdergame.game.repository;

import com.murdergame.game.entity.GameRoom;
import com.murdergame.game.entity.GameRoomState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    Optional<GameRoom> findByIdAndActiveTrue(Long id);
    List<GameRoom> findByStateAndActiveTrue(GameRoomState state);
    List<GameRoom> findByActiveTrue();
}