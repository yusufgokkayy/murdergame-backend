package com.murdergame.quiz.service.impl;

import com.murdergame.game.repository.GameRoomRepository;
import com.murdergame.quiz.dto.CreateQuestionRequest;
import com.murdergame.quiz.dto.QuestionResponse;
import com.murdergame.quiz.dto.SubmitAnswerRequest;
import com.murdergame.quiz.dto.SubmitAnswerResponse;
import com.murdergame.quiz.entity.Question;
import com.murdergame.quiz.entity.QuizAnswer;
import com.murdergame.quiz.repository.QuestionRepository;
import com.murdergame.quiz.repository.QuizAnswerRepository;
import com.murdergame.quiz.service.QuizService;
import com.murdergame.game.service.GameRoomService;
import com.murdergame.game.entity.GameRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuestionRepository questionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final GameRoomRepository gameRoomRepository;

    @Override
    public List<QuestionResponse> getQuestionsByGameRoom(Long gameRoomId) {
        return questionRepository.findByGameRoomId(gameRoomId)
                .stream()
                .map(this::mapToQuestionResponse)
                .toList();
    }

    @Override
    public QuestionResponse getQuestionById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Soru bulunamadı: " + questionId));
        return mapToQuestionResponse(question);
    }

    @Override
    public QuestionResponse createQuestion(Long gameRoomId, CreateQuestionRequest request) {
        // GameRoom'un varlığını kontrol et
        GameRoom gameRoom = gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new RuntimeException("GameRoom bulunamadı: " + gameRoomId));

        // Yeni Question entity oluştur - Builder pattern kullan
        Question question = Question.builder()
                .question(request.questionText())
                .optionA(request.optionA())
                .optionB(request.optionB())
                .optionC(request.optionC())
                .optionD(request.optionD())
                .correctAnswer(request.correctAnswer())
                .points(request.points())
                .gameRoom(gameRoom)
                .active(true)
                .build();

        // Veritabanına kaydet
        Question savedQuestion = questionRepository.save(question);

        return mapToQuestionResponse(savedQuestion);
    }

    @Override
    public SubmitAnswerResponse submitAnswer(Long gameRoomId, @RequestParam Long teamId, SubmitAnswerRequest request) {
        // İmplementasyon burada
        return null;
    }

    @Override
    public List<QuizAnswer> getGameRoomAnswers(Long gameRoomId) {
        return quizAnswerRepository.findByGameRoomId(gameRoomId);
    }

    @Override
    public List<QuizAnswer> getTeamAnswers(Long gameRoomId, Long teamId) {
        return quizAnswerRepository.findByGameRoomIdAndTeamId(gameRoomId, teamId);
    }

    // Helper method: Question entity'yi QuestionResponse'a dönüştür
    private QuestionResponse mapToQuestionResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getQuestion(),
                question.getPoints()
        );
    }
}