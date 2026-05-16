package com.propinsi.backend.mengelola_lomba.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalResultGantanganResponse {
    private Integer nomorGantangan;
    private Integer totalAjuan;
    private String hasilKoncer;
    private Integer totalPoin;
}
