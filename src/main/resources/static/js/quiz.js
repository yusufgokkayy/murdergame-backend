// GLOBAL VARIABLES
let stompClient = null;
let currentQuestion = null;
let selectedAnswer = null;
let teamId = null;
let userId = null;
let timeLeft = 60;
let timerInterval = null;

// PAGE LOAD
document.addEventListener('DOMContentLoaded', () => {
    // Token'dan userId ve teamId al
    const token = localStorage.getItem('accessToken');
    if (!token) {
        alert('Lütfen login olunuz');
        window.location.href = '/login';
        return;
    }

    // Decode JWT (simple)
    const payload = JSON.parse(atob(token.split('.')[1]));
    userId = payload.userId;
    teamId = payload.teamId;

    document.getElementById('teamName').textContent = `Takım: ${payload.teamId}`;

    // WebSocket bağlanması
    connectWebSocket(token);

    // Option buttons'a click listener
    document.querySelectorAll('.option-btn').forEach(btn => {
        btn.addEventListener('click', () => selectAnswer(btn));
    });

    // Submit button
    document.getElementById('submitBtn').addEventListener('click', submitAnswer);

    // Leaderboard'u yükle
    loadLeaderboard();
});

// WEBSOCKET BAĞLANTISI
function connectWebSocket(token) {
    const socket = new SockJS('/ws-quiz');
    stompClient = Stomp.over(socket);

    stompClient.connect(
        { Authorization: `Bearer ${token}` },
        (frame) => {
            console.log('WebSocket Connected:', frame);

            // 1. Soru başladığında dinle
            stompClient.subscribe('/topic/quiz/start', (message) => {
                const questionId = JSON.parse(message.body);
                loadQuestion(questionId);
            });

            // 2. Cevapları dinle
            stompClient.subscribe(`/topic/quiz/answers/${teamId}`, (message) => {
                const response = JSON.parse(message.body);
                showResult(response);
            });

            // 3. Soru bitti mesajı
            stompClient.subscribe('/topic/quiz/end', (message) => {
                endQuestion();
            });
        },
        (error) => {
            console.error('WebSocket Error:', error);
        }
    );
}

// SORUYU YÜKLEYEN FUNCTION
function loadQuestion(questionId) {
    fetch(`/api/quiz/questions/${questionId}`)
        .then(r => r.json())
        .then(data => {
            currentQuestion = data;
            displayQuestion(data);
            startTimer();
        });
}

// SORUYU GÖSTEREN FUNCTION
function displayQuestion(question) {
    document.getElementById('waitBox').classList.add('hidden');
    document.getElementById('questionBox').classList.remove('hidden');
    document.getElementById('resultBox').classList.add('hidden');

    document.getElementById('questionText').textContent = question.questionText;
    document.getElementById('optionA').textContent = question.optionA;
    document.getElementById('optionB').textContent = question.optionB;
    document.getElementById('optionC').textContent = question.optionC;
    document.getElementById('optionD').textContent = question.optionD;

    // Reset seçim
    selectedAnswer = null;
    document.querySelectorAll('.option-btn').forEach(btn => {
        btn.classList.remove('selected');
    });
    document.getElementById('submitBtn').disabled = true;

    timeLeft = 60;
}

// ŞIK SEÇ
function selectAnswer(btn) {
    document.querySelectorAll('.option-btn').forEach(b => b.classList.remove('selected'));
    btn.classList.add('selected');
    selectedAnswer = btn.dataset.answer;
    document.getElementById('submitBtn').disabled = false;
}

// CEVAP GÖNDER
function submitAnswer() {
    if (!selectedAnswer || !currentQuestion) {
        alert('Lütfen bir şık seçiniz');
        return;
    }

    const message = {
        teamId: teamId,
        questionId: currentQuestion.id,
        userAnswer: selectedAnswer
    };

    stompClient.send('/app/submit-answer', {}, JSON.stringify(message));
    document.getElementById('submitBtn').disabled = true;
}

// SONUÇ GÖSTER
function showResult(response) {
    clearInterval(timerInterval);

    document.getElementById('questionBox').classList.add('hidden');
    document.getElementById('resultBox').classList.remove('hidden');

    const isCorrect = response.isCorrect;
    const resultTitle = document.getElementById('resultTitle');
    const resultMessage = document.getElementById('resultMessage');
    const resultPoints = document.getElementById('resultPoints');

    if (isCorrect) {
        resultTitle.textContent = '✅ DOĞRU!';
        resultTitle.classList.remove('incorrect');
        resultTitle.classList.add('correct');
        resultMessage.textContent = 'Harika! Doğru cevabı bildiniz.';
        resultPoints.textContent = `+${response.pointsEarned} Puan`;
        updateTeamScore(response.pointsEarned);
    } else {
        resultTitle.textContent = '❌ YANLIŞ!';
        resultTitle.classList.remove('correct');
        resultTitle.classList.add('incorrect');
        resultMessage.textContent = 'Maalesef yanlış cevap verdiniz.';
        resultPoints.textContent = '+0 Puan';
    }

    setTimeout(() => {
        document.getElementById('resultBox').classList.add('hidden');
        document.getElementById('waitBox').classList.remove('hidden');
        loadLeaderboard();
    }, 3000);
}

// TIMER
function startTimer() {
    timeLeft = 60;
    timerInterval = setInterval(() => {
        timeLeft--;
        const percentage = (timeLeft / 60) * 100;
        document.getElementById('timerFill').style.width = percentage + '%';

        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            endQuestion();
        }
    }, 1000);
}

// SORU BITTI
function endQuestion() {
    clearInterval(timerInterval);
    document.getElementById('submitBtn').disabled = true;
    document.getElementById('questionBox').classList.add('hidden');
    document.getElementById('waitBox').classList.remove('hidden');
    loadLeaderboard();
}

// TAKIMIN SKORUNU GÜNCELLE
function updateTeamScore(points) {
    let score = parseInt(document.getElementById('userScore').textContent.split(': ')[1]) || 0;
    score += points;
    document.getElementById('userScore').textContent = `Skor: ${score}`;
}

// LEADERBOARD YÜKLE
function loadLeaderboard() {
    fetch('/api/leaderboard')
        .then(r => r.json())
        .then(data => {
            displayLeaderboard(data);
        })
        .catch(err => console.error('Leaderboard error:', err));
}

// LEADERBOARD GÖSTER
function displayLeaderboard(data) {
    const list = document.getElementById('leaderboardList');
    list.innerHTML = '';

    data.forEach((item, index) => {
        const div = document.createElement('div');
        div.className = 'leaderboard-item';

        if (index === 0) div.classList.add('top-1');
        else if (index === 1) div.classList.add('top-2');
        else if (index === 2) div.classList.add('top-3');

        div.innerHTML = `
            <span class="team-rank">#${index + 1} ${item.teamName}</span>
            <span class="team-score">${item.totalScore} Puan</span>
        `;
        list.appendChild(div);
    });
}