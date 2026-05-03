package com.murdergame.cluegame.repository;

import com.murdergame.cluegame.entity.ClueGameGuess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClueGameGuessRepository extends JpaRepository<ClueGameGuess, Long> {
    List<ClueGameGuess> findByClueGameIdAndTeamId(Long clueGameId, Long teamId);
    List<ClueGameGuess> findByClueGameId(Long clueGameId);
}