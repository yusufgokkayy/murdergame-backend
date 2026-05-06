package com.murdergame.quiz.repository;

import com.murdergame.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
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
}