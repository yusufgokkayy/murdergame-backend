package com.murdergame.cluegame.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.murdergame.team.entity.Team;
import com.murdergame.user.entity.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "clue_game_guesses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClueGameGuess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clue_game_id", nullable = false)
    private ClueGame clueGame;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String guessedName;

    @Column(nullable = false)
    private Boolean isCorrect;

    @Column(nullable = false)
    private Integer pointsEarned; // +50 veya -50

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime guessedAt = LocalDateTime.now();
}