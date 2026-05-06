package com.murdergame.quiz.service.impl;

import com.murdergame.game.entity.GameRoom;
import com.murdergame.game.entity.GameRoomState;
import com.murdergame.game.repository.GameRoomRepository;
import com.murdergame.leaderboard.service.LeaderboardService;
import com.murdergame.quiz.dto.QuestionBroadcastDTO;
import com.murdergame.quiz.dto.SubmitAnswerWSRequest;
import com.murdergame.quiz.entity.Question;
import com.murdergame.quiz.entity.QuizAnswer;
import com.murdergame.quiz.repository.QuestionRepository;
import com.murdergame.quiz.repository.QuizAnswerRepository;
import com.murdergame.quiz.service.QuizWebSocketService;
import com.murdergame.team.entity.Team;
import com.murdergame.team.repository.TeamRepository;
import com.murdergame.user.entity.User;
import com.murdergame.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizWebSocketServiceImpl implements QuizWebSocketService {

    private final GameRoomRepository gameRoomRepository;
    private final QuestionRepository questionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final LeaderboardService leaderboardService;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    private static final int QUESTION_DURATION_SECONDS = 60;

    @Transactional
    public void broadcastNextQuestion(Long roomId, Long adminId) {
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Sonraki index'e geç
        int nextIndex = (room.getCurrentQuestionIndex() == null)
                ? 0
                : room.getCurrentQuestionIndex() + 1;

        // Soruları id'ye göre sıralı çek
        List<Question> questions = questionRepository
                .findByGameRoomIdOrderByIdAsc(roomId);

        System.out.println("=== broadcastNextQuestion roomId: " + roomId + " nextIndex: " + nextIndex + " questions size: " + questions.size() + " ===");


        if (nextIndex >= questions.size()) {

            // ✅ YENİ — state'i DB'de güncelle
            room.setState(GameRoomState.QUIZ1_ENDED);
            gameRoomRepository.save(room);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/question",
                    Map.of("event", "QUIZ_FINISHED")
            );

            // ✅ YENİ — state değişimini de ayrıca broadcast et (opsiyonel ama temiz)
            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/state",
                    "QUIZ1_ENDED"
            );
            return;
        }


        Question question = questions.get(nextIndex);

        // GameRoom güncelle
        room.setCurrentQuestionId(question.getId());
        room.setCurrentQuestionIndex(nextIndex);
        room.setQuestionStartedAt(LocalDateTime.now());
        gameRoomRepository.save(room);

        // Broadcast — correctAnswer olmadan
        QuestionBroadcastDTO dto = new QuestionBroadcastDTO(
                question.getId(),
                nextIndex,
                question.getQuestion(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getPoints(),
                QUESTION_DURATION_SECONDS
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/question", dto
        );
    }

    @Transactional
    public void submitAnswer(Long roomId, Long userId,
                             SubmitAnswerWSRequest request) {

        System.out.println("=== submitAnswer roomId: " + roomId + " userId: " + userId + " questionId: " + request.questionId() + " ===");

        if (userId == null) { System.out.println("=== NULL userId ==="); return; }

        GameRoom room = gameRoomRepository.findById(roomId).orElseThrow();
        System.out.println("=== room currentQuestionId: " + room.getCurrentQuestionId() + " ===");

        if (room.getQuestionStartedAt() == null) { System.out.println("=== questionStartedAt null ==="); return; }

        long elapsed = ChronoUnit.SECONDS.between(room.getQuestionStartedAt(), LocalDateTime.now());
        System.out.println("=== elapsed: " + elapsed + " ===");
        if (elapsed > QUESTION_DURATION_SECONDS) { System.out.println("=== SURE DOLDU ==="); return; }

        if (!request.questionId().equals(room.getCurrentQuestionId())) {
            System.out.println("=== QUESTION ID MISMATCH: request=" + request.questionId() + " current=" + room.getCurrentQuestionId() + " ===");
            return;
        }

        User user = userRepository.findById(userId).orElseThrow();
        System.out.println("=== user team: " + user.getTeam() + " ===");

        Team team = user.getTeam();
        if (team == null) { System.out.println("=== TEAM NULL ==="); return; }

        Question question = questionRepository.findById(request.questionId()).orElseThrow();
        boolean alreadyAnswered = quizAnswerRepository.existsByTeamAndQuestion(team, question);
        System.out.println("=== alreadyAnswered: " + alreadyAnswered + " ===");
        if (alreadyAnswered) { System.out.println("=== ZATEN CEVAPLANDI ==="); return; }

        System.out.println("=== KAYIT YAPILIYOR ===");

        boolean isCorrect = question.getCorrectAnswer()
                .equalsIgnoreCase(request.selectedAnswer());

        int bet = (request.betAmount() != null)
                ? Math.max(0, Math.min(100, request.betAmount()))  // 0-100 arası zorla
                : 0;

        int pointsEarned;

        if (bet == 0) {
            pointsEarned = isCorrect ? question.getPoints() : 0;
        } else {
            pointsEarned = isCorrect ? bet : -bet;
        }

        QuizAnswer answer = QuizAnswer.builder()
                .gameRoom(room)
                .team(team)
                .question(question)
                .teamAnswer(request.selectedAnswer())
                .isCorrect(isCorrect)
                .betAmount(bet)
                .pointsEarned(pointsEarned)
                .build();

        quizAnswerRepository.save(answer);

        System.out.println("=== ANSWER SAVED, calling leaderboard ===");
        var leaderboard = leaderboardService.getLeaderboard();
        System.out.println("=== LEADERBOARD: " + leaderboard +" ===");
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/leaderboard", leaderboard
        );
        System.out.println("=== BROADCAST SENT ===");
    }

    @Transactional
    public void endCurrentQuestion(Long roomId) {
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow();

        // Doğru cevabı broadcast et
        if (room.getCurrentQuestionId() != null) {
            Question q = questionRepository
                    .findById(room.getCurrentQuestionId()).orElseThrow();

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/question-result",
                    Map.of(
                            "event", "QUESTION_ENDED",
                            "correctAnswer", q.getCorrectAnswer(),
                            "questionId", q.getId()
                    )
            );
        }
    }
}