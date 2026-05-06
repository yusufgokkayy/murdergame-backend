package com.murdergame.auth.service;

import com.murdergame.auth.dto.*;

import java.util.List;

public interface AuthService {
    AuthResponse adminLogin(AdminLoginRequest request);
    AuthResponse userLogin(UserLoginRequest request);
    AuthResponse userRegister(UserRegisterRequest request);
    // refresh() METHOD SİL
    List<UserResponse> getAllUsers();
}