package com.murdergame.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRegisterRequest(
        @NotBlank(message = "Username boş olamaz")
        String username
) {}