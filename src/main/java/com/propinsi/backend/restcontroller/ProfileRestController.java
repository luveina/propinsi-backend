package com.propinsi.backend.restcontroller;

import com.propinsi.backend.restdto.request.UpdatePasswordRequest;
import com.propinsi.backend.restdto.response.BaseResponse;
import com.propinsi.backend.restdto.response.UserProfileResponse;
import com.propinsi.backend.service.ProfileService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/profile")
public class ProfileRestController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<BaseResponse<UserProfileResponse>> getProfile() {
        // Mengambil username dari JWT token yang sudah divalidasi oleh Spring Security
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        UserProfileResponse profile = profileService.getUserProfile(username);
        
        return ResponseEntity.ok(BaseResponse.success(profile, "Data profil berhasil diambil"));
    }

    // Tambahkan endpoint PBI-05 di sini
    @PutMapping("/password")
    public ResponseEntity<BaseResponse<Void>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            BindingResult bindingResult
    ) {
        // Validasi format input (@NotBlank, @Size)
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input tidak valid");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        profileService.updatePassword(username, request);

        return ResponseEntity.ok(BaseResponse.success(null, "Password berhasil diperbarui"));
    }
}