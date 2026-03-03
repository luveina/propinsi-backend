package com.propinsi.backend.restdto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Password Baru wajib diisi")
    @Size(min = 6, message = "Password minimal 6 karakter")
    private String newPassword;

    @NotBlank(message = "Konfirmasi Password wajib diisi")
    private String confirmPassword;
}