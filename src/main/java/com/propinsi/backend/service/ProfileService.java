package com.propinsi.backend.service;

import com.propinsi.backend.model.User;
import com.propinsi.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Map<String, String> getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        Map<String, String> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("namaLengkap", user.getNamaLengkap());
        response.put("nomorWhatsApp", user.getNomorWhatsApp());
        response.put("role", user.getRole());
        return response;
    }

    public void updatePassword(String username, Map<String, String> data) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (!passwordEncoder.matches(data.get("oldPassword"), user.getPassword())) {
            throw new RuntimeException("Password lama Anda tidak sesuai");
        }

        user.setPassword(passwordEncoder.encode(data.get("newPassword")));
        userRepository.save(user);
    }
}