package com.propinsi.backend.service;

import com.propinsi.backend.model.*;
import com.propinsi.backend.repository.*;
import com.propinsi.backend.restdto.request.*;
import com.propinsi.backend.restdto.response.JwtResponse;
import com.propinsi.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired private UserRepository userDb;
    @Autowired private JwtBlacklistRepository blacklistRepository;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public JwtResponse login(LoginRequest req) {
        User userCheck = userDb.findByUsername(req.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Akun tidak ditemukan."));
        
        if ("Inactive".equalsIgnoreCase(userCheck.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Akun tidak ditemukan.");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);
            User user = (User) auth.getPrincipal();

            user.setLastLogin(LocalDateTime.now());
            userDb.save(user);

            String jwt = jwtUtils.generateToken(user);
            return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getFullName(), 
                                   user.getRole().name(), user.isFirstLogin());
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username atau Password salah");
        }
    }

    @Override
    public void register(RegisterRequest req) {
        if (userDb.existsByUsername(req.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username sudah digunakan!");
        }
        User user = User.builder()
                .fullName(req.getFullName()).username(req.getUsername())
                .phoneNumber(req.getPhoneNumber()).password(passwordEncoder.encode(req.getPassword()))
                .role(Role.PESERTA).isFirstLogin(false).status("Active").build();
        userDb.save(user);
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            blacklistRepository.save(new JwtBlacklist(token, LocalDateTime.now().plusHours(24)));
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    public void updateInitialPassword(String username, ChangePasswordRequest req) {
        User user = userDb.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Akun tidak ditemukan."));
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Konfirmasi password tidak sesuai!");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setFirstLogin(false);
        userDb.save(user);
    }
}