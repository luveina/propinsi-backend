package com.propinsi.backend.restdto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Nama Lengkap wajib diisi")
    private String fullName;

    @NotBlank(message = "Username wajib diisi")
    @Size(min = 4, message = "Username minimal 4 karakter")
    private String username;

    @NotBlank(message = "Nomor Telepon wajib diisi")
    @Pattern(regexp = "^08\\d{8,11}$", message = "Nomor telepon harus dimulai dengan 08 dan terdiri dari 10-13 digit")
    private String phoneNumber;

    @NotBlank(message = "Password wajib diisi")
    @Size(min = 6, message = "Password minimal 6 karakter")
    private String password;

    @NotBlank(message = "Konfirmasi Password wajib diisi")
    private String confirmPassword;
}