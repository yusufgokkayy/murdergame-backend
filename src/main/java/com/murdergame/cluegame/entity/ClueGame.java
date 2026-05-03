package com.murdergame.cluegame.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.murdergame.team.entity.Team;
import com.murdergame.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clue_games")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClueGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String killerName; // Katilin gerçek adı

    @Column(nullable = false, columnDefinition = "TEXT")
    private String trueStory; // Gerçek hikaye

    @ElementCollection
    @CollectionTable(name = "clue_game_clues", joinColumns = @JoinColumn(name = "clue_game_id"))
    @OrderColumn(name = "clue_order")
    private List<String> clues; // İpuçları listesi

    @OneToMany(mappedBy = "clueGame", cascade = CascadeType.ALL)
    private List<ClueGameGuess> guesses; // Tüm tahminler

    @OneToMany(mappedBy = "clueGame", cascade = CascadeType.ALL)
    private List<ClueGameFinalAnswer> finalAnswers; // 30 dakika sonraki cevaplar

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}