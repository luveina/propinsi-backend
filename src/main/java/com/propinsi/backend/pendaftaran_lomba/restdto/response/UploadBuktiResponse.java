package com.propinsi.backend.pendaftaran_lomba.restdto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadBuktiResponse {
    private UUID reservasiId;
    private String status;
    private String urlBuktiPembayaran;
}
