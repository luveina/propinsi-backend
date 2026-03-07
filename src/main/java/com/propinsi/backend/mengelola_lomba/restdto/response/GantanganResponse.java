package com.propinsi.backend.mengelola_lomba.restdto.response;

import lombok.*;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class GantanganResponse {
    private UUID id;
    private Integer nomorGantangan;
    private Boolean isAvailable;
    private UserSummaryResponse peserta;
}
