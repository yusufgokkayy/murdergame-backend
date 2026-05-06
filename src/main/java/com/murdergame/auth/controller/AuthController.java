package com.murdergame.auth.controller;

import com.murdergame.auth.dto.*;
import com.murdergame.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/admin/login")
    public AuthResponse adminLogin(@RequestBody AdminLoginRequest request) {
        return authService.adminLogin(request);
    }

    @PostMapping("/user/login")
    public AuthResponse userLogin(@RequestBody UserLoginRequest request) {
        return authService.userLogin(request);
    }

    @PostMapping("/user/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse userRegister(@RequestBody UserRegisterRequest request) {
        return authService.userRegister(request);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    // /refresh endpoint SİL
}