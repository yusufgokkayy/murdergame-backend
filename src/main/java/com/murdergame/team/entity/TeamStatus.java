package com.murdergame.team.entity;

public enum TeamStatus {
    READY,             // Takım hazır
    NOT_READY,         // Takım hazır değil
    ANSWERED,          // Quiz'e cevap verdi
    WAITING_ANSWER,    // Quiz cevabı bekliyor
    ELIMINATED,        // ClueGame'de elendi
    ACTIVE             // Oyuna aktif katılıyor
}