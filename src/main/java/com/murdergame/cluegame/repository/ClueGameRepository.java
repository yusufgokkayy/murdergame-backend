package com.murdergame.cluegame.repository;

import com.murdergame.cluegame.entity.ClueGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClueGameRepository extends JpaRepository<ClueGame, Long> {
    Optional<ClueGame> findByIdAndIsActiveTrue(Long id);
}