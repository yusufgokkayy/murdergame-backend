package com.murdergame.quiz.repository;

import com.murdergame.quiz.entity.Question;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // ✅ YENİ - GameRoom bazlı sorgu
    List<Question> findByGameRoomId(Long gameRoomId);

    List<Question> findByActiveTrue();
    Optional<Question> findByIdAndActiveTrue(Long id);

    List<Question> findByGameRoomIdOrderByIdAsc(Long gameRoomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Question q WHERE q.gameRoom.id = :gameRoomId")
    void deleteByGameRoomId(@Param("gameRoomId") Long gameRoomId);

    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.active = false WHERE q.gameRoom.id = :gameRoomId")
    void deactivateByGameRoomId(@Param("gameRoomId") Long gameRoomId);

    List<Question> findByGameRoomIdAndActiveTrue(Long gameRoomId);

    List<Question> findByGameRoomIdAndActiveTrueOrderByIdAsc(Long roomId);
}