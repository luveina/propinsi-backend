package com.propinsi.backend.service;

import com.propinsi.backend.model.User;
import com.propinsi.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void resetPassword(Long id) {
        // Cari user berdasarkan ID, jika tidak ada lempar 404 (Sesuai AC BE point 4)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        // Reset password ke default "Silobur123!" dan di-hash (Sesuai AC BE point 2)
        user.setPassword(passwordEncoder.encode("Silobur123!"));
        
        // Opsional: Set isFirstLogin ke true agar user wajib ganti password lagi saat login
        user.setFirstLogin(true);

        userRepository.save(user);
    }
}