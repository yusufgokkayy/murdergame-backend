package com.murdergame.game.entity;

public enum GameRoomState {
    WAITING,           // Bekleme (takımlar hazırlanıyor)
    QUIZ1,             // Quiz 1 devam ediyor
    QUIZ1_ENDED,       // Quiz 1 bitti
    QUIZ2,             // Quiz 2 devam ediyor
    QUIZ2_ENDED,       // Quiz 2 bitti
    CLUEGAME,          // ClueGame devam ediyor (30 dakika)
    CLUEGAME_FINAL,    // Final cevap zamanı
    CLUEGAME_ENDED,    // ClueGame bitti
    ENDED              // Oyun tamamen bitti
}