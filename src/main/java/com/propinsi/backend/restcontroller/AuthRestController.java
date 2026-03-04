package com.propinsi.backend.restcontroller;

import com.propinsi.backend.restdto.request.ChangePasswordRequest;
import com.propinsi.backend.restdto.request.LoginRequest;
import com.propinsi.backend.restdto.request.RegisterRequest;
import com.propinsi.backend.restdto.response.BaseResponse;
import com.propinsi.backend.restdto.response.JwtResponse;
import com.propinsi.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private AuthService authService;

    // PBI 01: Login
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest req, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input tidak valid");
        }
        
        JwtResponse jwtResponse = authService.login(req);
        return ResponseEntity.ok(BaseResponse.success(jwtResponse, "Login Berhasil"));
    }

    @PutMapping("/update-initial-password")
    public ResponseEntity<BaseResponse<Void>> updateInitialPassword(
            @Valid @RequestBody ChangePasswordRequest req, 
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input tidak valid");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        authService.updateInitialPassword(username, req);

        return ResponseEntity.ok(BaseResponse.success(null, "Password awal berhasil diubah. Akun Anda kini aktif sepenuhnya."));
    }

    // PBI 02: Register
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<Void>> register(@Valid @RequestBody RegisterRequest req, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors);
        }

        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(null, "Registrasi Berhasil"));
    }

    // PBI 03: Logout
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(BaseResponse.success(null, "Logout Berhasil"));
    }
}