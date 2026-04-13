package com.propinsi.backend.penjurian.restdto.response;

import com.propinsi.backend.mengelola_lomba.model.GantanganStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringGantanganResponse {
    private UUID id;
    private Integer nomorGantangan;
    private GantanganStatus status;
    private Integer warningCount;
    private Boolean isBooked;
}
