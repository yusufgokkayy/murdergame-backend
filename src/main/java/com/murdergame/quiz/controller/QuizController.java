package com.murdergame.quiz.controller;

import com.murdergame.quiz.dto.CreateQuestionRequest;
import com.murdergame.quiz.dto.QuestionResponse;
import com.murdergame.quiz.dto.SubmitAnswerRequest;
import com.murdergame.quiz.dto.SubmitAnswerResponse;
import com.murdergame.quiz.entity.QuizAnswer;
import com.murdergame.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // User: Oda'daki soruları getir
    @GetMapping("/room/{gameRoomId}/questions")
   // @PreAuthorize("hasRole('USER')")
    public List<QuestionResponse> getQuestionsByGameRoom(@PathVariable Long gameRoomId) {
        return quizService.getQuestionsByGameRoom(gameRoomId);
    }

    // User: Specific soru getir
    @GetMapping("/question/{questionId}")
    //@PreAuthorize("hasRole('USER')")
    public QuestionResponse getQuestionById(@PathVariable Long questionId) {
        return quizService.getQuestionById(questionId);
    }

    @PostMapping("/room/{gameRoomId}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> createQuestion(
            @PathVariable Long gameRoomId,
            @RequestBody CreateQuestionRequest request) {
        QuestionResponse response = quizService.createQuestion(gameRoomId, request);
        URI location = URI.create("/api/quiz/question/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/room/{gameRoomId}/questions/multiple")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QuestionResponse>> createMultipleQuestions(
            @PathVariable Long gameRoomId,
            @RequestBody List<CreateQuestionRequest> requests) {
        List<QuestionResponse> responses = quizService.createMultipleQuestions(gameRoomId, requests);
        return ResponseEntity.ok(responses);
    }

    // User: Cevap gönder
//    @PostMapping("/room/{gameRoomId}/submit")
//    @PreAuthorize("hasRole('USER')")
//    public SubmitAnswerResponse submitAnswer(
//            @PathVariable Long gameRoomId,
//            @RequestParam Long teamId,
//            @RequestBody SubmitAnswerRequest request) {
//        return quizService.submitAnswer(gameRoomId, teamId, request);
//    }

    // Admin: Oda'daki tüm cevapları getir
    @GetMapping("/room/{gameRoomId}/answers")
    @PreAuthorize("hasRole('ADMIN')")
    public List<QuizAnswer> getGameRoomAnswers(@PathVariable Long gameRoomId) {
        return quizService.getGameRoomAnswers(gameRoomId);
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteQuestion(
            @PathVariable Long questionId) {

        quizService.deleteQuestion(questionId);

        return ResponseEntity.ok("Soru silindi.");
    }

    // Admin: Takımın cevaplarını getir
    @GetMapping("/room/{gameRoomId}/team/{teamId}/answers")
    @PreAuthorize("hasRole('ADMIN')")
    public List<QuizAnswer> getTeamAnswers(
            @PathVariable Long gameRoomId,
            @PathVariable Long teamId) {
        return quizService.getTeamAnswers(gameRoomId, teamId);
    }
}