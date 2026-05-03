package com.murdergame.auth.service;

import com.murdergame.auth.dto.*;

public interface AuthService {
    AuthResponse adminLogin(AdminLoginRequest request);
    AuthResponse userLogin(UserLoginRequest request);
    AuthResponse userRegister(UserRegisterRequest request);
    // refresh() METHOD SİL
}