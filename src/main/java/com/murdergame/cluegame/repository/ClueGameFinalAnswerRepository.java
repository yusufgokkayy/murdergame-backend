package com.murdergame.cluegame.repository;

import com.murdergame.cluegame.entity.ClueGameFinalAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClueGameFinalAnswerRepository extends JpaRepository<ClueGameFinalAnswer, Long> {
    Optional<ClueGameFinalAnswer> findByClueGameIdAndTeamId(Long clueGameId, Long teamId);
}