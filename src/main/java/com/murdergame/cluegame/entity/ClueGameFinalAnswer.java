package com.murdergame.cluegame.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.murdergame.team.entity.Team;
import java.time.LocalDateTime;

@Entity
@Table(name = "clue_game_final_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClueGameFinalAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clue_game_id", nullable = false)
    private ClueGame clueGame;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private String finalGuess;

    @Column(nullable = false)
    private Boolean isCorrect;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime answeredAt = LocalDateTime.now();
}