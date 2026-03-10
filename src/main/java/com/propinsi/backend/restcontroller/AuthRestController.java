package com.propinsi.backend.restcontroller;

import com.propinsi.backend.restdto.request.*;
import com.propinsi.backend.restdto.response.*;
import com.propinsi.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
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
    @Autowired private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest req, BindingResult res) {
        if (res.hasErrors()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input tidak valid");
        return ResponseEntity.ok(BaseResponse.success(authService.login(req), "Login Berhasil"));
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<Void>> register(@Valid @RequestBody RegisterRequest req, BindingResult res) {
        if (res.hasErrors()) {
            String msg = res.getFieldErrors().stream().map(e -> e.getDefaultMessage()).collect(Collectors.joining(", "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(null, "Registrasi Berhasil"));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesi tidak valid");
        }
        authService.logout(token);
        return ResponseEntity.ok(BaseResponse.success(null, "Logout Berhasil"));
    }

    @PutMapping("/update-initial-password")
    public ResponseEntity<BaseResponse<Void>> updatePassword(@Valid @RequestBody ChangePasswordRequest req, BindingResult res) {
        if (res.hasErrors()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input tidak valid");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.updateInitialPassword(username, req);
        return ResponseEntity.ok(BaseResponse.success(null, "Password berhasil diubah."));
    }
}