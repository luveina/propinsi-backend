package com.propinsi.backend.mengelola_lomba.restdto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AssignJuriRequest {
    @NotEmpty(message = "Daftar ID Juri tidak boleh kosong")
    private List<Long> juriIds;
}