package com.propinsi.backend.service;


import com.propinsi.backend.restdto.request.ChangePasswordRequest;
import com.propinsi.backend.restdto.request.LoginRequest;
import com.propinsi.backend.restdto.request.RegisterRequest;
import com.propinsi.backend.restdto.response.JwtResponse;

public interface AuthService {
    JwtResponse login(LoginRequest loginRequest);
    void register(RegisterRequest registerRequest);
    void updateInitialPassword(String username, ChangePasswordRequest req);
    void logout(String token);
}