package com.propinsi.backend.service;

import com.propinsi.backend.model.Role;
import com.propinsi.backend.model.User;
import com.propinsi.backend.repository.UserRepository;
import com.propinsi.backend.restdto.request.AdminRegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User createAccount(AdminRegisterRequest dto, String adminName) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username sudah terdaftar");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .fullName(dto.getFullName())
                .phoneNumber(dto.getPhoneNumber())
                .role(dto.getRole())
                .status("Active")
                .password(encoder.encode("Silobur123!")) // Default password
                .createdBy(adminName)
                .updatedBy(adminName)
                .build();

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Update simple fields of an existing user (name, phone, role).
     * AdminName is recorded in updatedBy.
     */
    public User updateAccount(Long id, AdminRegisterRequest dto, String adminName) {
        User user = findById(id);
        if (user == null) {
            return null;
        }
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        try {
            user.setRole(dto.getRole());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex.getMessage());
        }
        user.setUpdatedBy(adminName);
        return userRepository.save(user);
    }

    /**
     * Soft-delete a user by setting status to Inactive.
     * @param id user id
     * @param adminName name of admin performing action
     * @return updated user or null if not found
     */
    public User deactivateUser(Long id, String adminName) {
        User user = findById(id);
        if (user == null) {
            return null;
        }
        user.setStatus("Inactive");
        user.setUpdatedBy(adminName);
        return userRepository.save(user);
    }
}