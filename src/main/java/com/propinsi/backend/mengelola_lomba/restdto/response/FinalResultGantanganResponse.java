package com.propinsi.backend.mengelola_lomba.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalResultGantanganResponse {
    private Integer nomorGantangan;
    private Long totalAjuan;
    private String hasilKoncer;
    private Integer totalPoin;
}
