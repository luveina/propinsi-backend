package com.propinsi.backend.controller;

import com.propinsi.backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> getProfile(Authentication auth) {
        String username = (auth != null) ? auth.getName() : "Mihumihuily";
        return ResponseEntity.ok(profileService.getUserProfile(username));
    }

    @PutMapping("/profile/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> payload, Authentication auth) {
        try {
            String username = (auth != null) ? auth.getName() : "Mihumihuily";
            profileService.updatePassword(username, payload);
            return ResponseEntity.ok("Sukses");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}