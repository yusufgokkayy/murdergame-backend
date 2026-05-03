package com.murdergame.quiz.repository;

import com.murdergame.quiz.entity.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    // ✅ YENİ - GameRoom bazlı sorgu
    List<QuizAnswer> findByGameRoomId(Long gameRoomId);
    List<QuizAnswer> findByGameRoomIdAndTeamId(Long gameRoomId, Long teamId);

    // Eski
    List<QuizAnswer> findByTeamId(Long teamId);
}