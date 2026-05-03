package com.murdergame.user.validator;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class UsernameValidator {

    // 3-20 karakter, a-z, A-Z, 0-9, _, - sadece
    // başında/sonunda harf veya rakam olmalı
    private static final Pattern VALID_USERNAME =
            Pattern.compile("^[a-zA-Z0-9]([a-zA-Z0-9_-]{1,18}[a-zA-Z0-9])?$");

    // Küfürlü / Rezerve Kelimeleri Blocklist'e al
    private static final String[] BLOCKLIST = {
            "admin", "root", "system", "moderator", "administrator",
            "bok", "sik", "am", "bacak", "salak", "orospu", "piç", "it",
            "test", "user123", "admin123", "password", "123456",
            "murdergame", "killer", "victim", "null", "undefined",
            "fuck", "shit", "bitch", "ass", "cock", "dick",
            "xxx", "porn", "sex", "rape", "kill", "die"
    };

    /**
     * Username'in valid olup olmadığını kontrol eder
     */
    public boolean isValid(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }

        if (!VALID_USERNAME.matcher(username).matches()) {
            return false;
        }

        String lowerUsername = username.toLowerCase();
        for (String blocked : BLOCKLIST) {
            if (lowerUsername.contains(blocked)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validation hatası varsa açıklayıcı mesaj döndürür
     */
    public String getValidationError(String username) {
        if (username == null || username.isEmpty()) {
            return "Username boş olamaz";
        }

        if (username.length() < 3) {
            return "Username minimum 3 karakter olmalı";
        }

        if (username.length() > 20) {
            return "Username maksimum 20 karakter olmalı";
        }

        if (!VALID_USERNAME.matcher(username).matches()) {
            return "Username sadece harf, rakam, _ ve - içerebilir (başında ve sonunda harf veya rakam olmalı)";
        }

        String lowerUsername = username.toLowerCase();
        for (String blocked : BLOCKLIST) {
            if (lowerUsername.contains(blocked)) {
                return "Bu username kullanılamaz (uygunsuz içerik veya rezerve kelime)";
            }
        }

        return null; // Valid
    }
}