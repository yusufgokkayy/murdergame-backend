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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    // --- YENİ: Backend timer için Scheduler ve oda bazlı sayaçları tutacağımız Map ---
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<Long, ScheduledFuture<?>> roomTimers = new ConcurrentHashMap<>();
    // ---------------------------------------------------------------------------------

    @Transactional
    public void broadcastNextQuestion(Long roomId, Long adminId) {
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // --- YENİ GÜVENLİK: Admin butona çift tıklarsa veya yanlışlıkla tekrar basarsa önceki sayacı iptal et ---
        ScheduledFuture<?> existingTimer = roomTimers.get(roomId);
        if (existingTimer != null && !existingTimer.isDone()) {
            existingTimer.cancel(false);
            System.out.println("=== ÖNCEKİ SAYAÇ İPTAL EDİLDİ (Yeni soruya geçildi) ===");
        }
        // ----------------------------------------------------------------------------------------------------

        // Sonraki index'e geç
        // Soruları id'ye göre sıralı çek (sadece aktif olanlar)
        List<Question> questions = questionRepository
                .findByGameRoomIdAndActiveTrueOrderByIdAsc(roomId);

// currentQuestionId'ye göre gerçek pozisyonu bul
        int nextIndex;
        if (room.getCurrentQuestionId() == null) {
            nextIndex = 0;
        } else {
            int currentPos = -1;
            for (int i = 0; i < questions.size(); i++) {
                if (questions.get(i).getId().equals(room.getCurrentQuestionId())) {
                    currentPos = i;
                    break;
                }
            }
            nextIndex = currentPos + 1;
        }
        System.out.println("=== broadcastNextQuestion roomId: " + roomId + " nextIndex: " + nextIndex + " questions size: " + questions.size() + " ===");

        if (nextIndex >= questions.size()) {
            room.setState(GameRoomState.QUIZ1_ENDED);
            gameRoomRepository.save(room);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/question",
                    Map.of("event", "QUIZ_FINISHED")
            );

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

        // --- YENİ: 60 saniye sonrasına timer kur. Süre bitince otomatik endCurrentQuestion çalışsın! ---
        ScheduledFuture<?> newTimer = scheduler.schedule(() -> {
            try {
                System.out.println("=== SÜRE DOLDU! OTOMATİK OLARAK SORU BİTİRİLİYOR (Room: " + roomId + ") ===");
                // Zamanlayıcı thread içinde olduğu için transaction proxy'sini kendi üzerinden tetiklemek adına metodu direkt çağırıyoruz
                endCurrentQuestion(roomId);
            } catch (Exception e) {
                System.err.println("Otomatik soru bitirme sırasında hata: " + e.getMessage());
            }
        }, QUESTION_DURATION_SECONDS, TimeUnit.SECONDS);

        // Timer'ı haritaya kaydet
        roomTimers.put(roomId, newTimer);
        // ---------------------------------------------------------------------------------------------
    }

    @Transactional
    public void submitAnswer(Long roomId, Long userId,
                             SubmitAnswerWSRequest request) {

        System.out.println("=== submitAnswer roomId: " + roomId + " userId: " + userId + " questionId: " + request.questionId() + " ===");

        if (userId == null) { System.out.println("=== NULL userId ==="); return; }

        GameRoom room = gameRoomRepository.findById(roomId).orElseThrow();

        if (room.getQuestionStartedAt() == null) { System.out.println("=== questionStartedAt null ==="); return; }

        long elapsed = ChronoUnit.SECONDS.between(room.getQuestionStartedAt(), LocalDateTime.now());
        if (elapsed > QUESTION_DURATION_SECONDS) { System.out.println("=== SURE DOLDU (Sunucu Zamanı) ==="); return; }

        if (!request.questionId().equals(room.getCurrentQuestionId())) {
            System.out.println("=== QUESTION ID MISMATCH: request=" + request.questionId() + " current=" + room.getCurrentQuestionId() + " ===");
            return;
        }

        User user = userRepository.findById(userId).orElseThrow();
        Team team = user.getTeam();
        if (team == null) { System.out.println("=== TEAM NULL ==="); return; }

        // --- YENİ: SÖZCÜ KONTROLÜ ---
        if (team.getSpokespersonId() == null || !team.getSpokespersonId().equals(userId)) {
            System.out.println("=== YETKİSİZ CEVAP: Sadece takım sözcüsü cevap verebilir. (UserId: " + userId + ") ===");
            return; // Sözcü değilse işlemi sonlandır
        }
        // ----------------------------

        Question question = questionRepository.findById(request.questionId()).orElseThrow();
        boolean alreadyAnswered = quizAnswerRepository.existsByTeamAndQuestion(team, question);
        if (alreadyAnswered) { System.out.println("=== ZATEN CEVAPLANDI ==="); return; }

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
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/leaderboard", leaderboard
        );
        System.out.println("=== BROADCAST SENT ===");
    }

    @Transactional
    public void endCurrentQuestion(Long roomId) {
        // İşlem bittiğinde temizlik yap
        roomTimers.remove(roomId);

        GameRoom room = gameRoomRepository.findById(roomId).orElse(null);
        if (room == null) return;

        // Doğru cevabı broadcast et
        if (room.getCurrentQuestionId() != null) {
            Question q = questionRepository
                    .findById(room.getCurrentQuestionId()).orElse(null);

            if (q != null) {
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
}