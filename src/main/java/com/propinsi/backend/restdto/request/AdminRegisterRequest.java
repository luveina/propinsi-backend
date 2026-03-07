package com.propinsi.backend.restdto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.propinsi.backend.model.Role;
import lombok.Data;

@Data
public class AdminRegisterRequest {
    @NotBlank(message = "Username wajib diisi")
    private String username;

    @NotBlank(message = "Nama lengkap wajib diisi")
    private String fullName;

    @NotBlank(message = "Nomor telepon wajib diisi")
    private String phoneNumber;

    @NotNull
    private Role role;
}