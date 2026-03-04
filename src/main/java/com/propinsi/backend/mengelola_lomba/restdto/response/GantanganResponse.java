package com.propinsi.backend.mengelola_lomba.restdto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class GantanganResponse {
    private UUID id;
    private Integer nomorGantangan;
    private Boolean isAvailable;
}