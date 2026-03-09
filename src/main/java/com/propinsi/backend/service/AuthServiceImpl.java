package com.propinsi.backend.service;

import com.propinsi.backend.model.Role;
import com.propinsi.backend.model.User;
import com.propinsi.backend.repository.UserRepository;
import com.propinsi.backend.restdto.request.ChangePasswordRequest;
import com.propinsi.backend.restdto.request.LoginRequest;
import com.propinsi.backend.restdto.request.RegisterRequest;
import com.propinsi.backend.restdto.response.JwtResponse;
import com.propinsi.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService{

    @Autowired private UserRepository userDb;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public JwtResponse login(LoginRequest req) {
        User userCheck = userDb.findByUsername(req.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Akun tidak ditemukan."));
        
        if ("Inactive".equalsIgnoreCase(userCheck.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Akun tidak ditemukan.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        User user = (User) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(user);

        return new JwtResponse(
                jwt, user.getId(), user.getUsername(), user.getFullName(), 
                user.getRole().name(), user.isFirstLogin() 
        );
    }

    @Override
    public void updateInitialPassword(String username, ChangePasswordRequest req) {
        User user = userDb.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Akun tidak ditemukan."));

        if (!user.isFirstLogin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Akun Anda sudah aktif. Silakan gunakan fitur 'Ganti Password' di menu Profil.");
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Konfirmasi password tidak sesuai!");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));

        user.setFirstLogin(false);

        userDb.save(user);
    }

    // register
    @Override
    public void register(RegisterRequest req) {
        if (userDb.existsByUsername(req.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username sudah digunakan!");
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password dan Konfirmasi Password tidak cocok!");
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .username(req.getUsername())
                .phoneNumber(req.getPhoneNumber())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.PESERTA) // default
                .isFirstLogin(false) // no change pw
                .status("Active")
                .build();

        userDb.save(user);
    }
}