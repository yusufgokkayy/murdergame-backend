package com.murdergame.game.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.murdergame.team.entity.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "game_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GameRoomState state = GameRoomState.WAITING;

    @JsonIgnore
    @OneToMany(mappedBy = "gameRoom", cascade = CascadeType.ALL)
    private List<Team> teams;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @Builder.Default
    private Boolean active = true;
}