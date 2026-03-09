package com.propinsi.backend.restdto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.propinsi.backend.model.Role;
import lombok.Data;

@Data
public class AdminRegisterRequest {
    @NotBlank(message = "Username wajib diisi")
    private String username;

    @NotBlank(message = "Nama lengkap wajib diisi")
    private String fullName;

    @NotBlank(message = "Nomor telepon wajib diisi")
    @Pattern(regexp = "^08\\d{8,11}$", message = "Nomor telepon harus dimulai dengan 08 dan terdiri dari 10-13 digit")
    private String phoneNumber;

    @NotNull
    private Role role;
}