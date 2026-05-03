package com.murdergame.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank String teamNo,
        @NotBlank String teamPassword,
        @NotBlank String username
) {}