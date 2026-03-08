package com.propinsi.backend.service;

import com.propinsi.backend.model.User;
import com.propinsi.backend.repository.UserRepository;
import com.propinsi.backend.restdto.request.UpdatePasswordRequest;
import com.propinsi.backend.restdto.response.UserProfileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        return UserProfileResponse.builder()
                .fullName(user.getFullName())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void updatePassword(String username, UpdatePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        // 1. Validasi Password Lama (AC BE point 2)
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password lama Anda tidak sesuai");
        }

        // 2. Validasi Konfirmasi Password Baru
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Konfirmasi password baru tidak cocok");
        }

        // 3. Enkripsi Password Baru (AC BE point 3)
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        
        // Simpan ke database
        userRepository.save(user);
    }
}