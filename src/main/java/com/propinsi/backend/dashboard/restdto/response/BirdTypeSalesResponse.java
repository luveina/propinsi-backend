package com.propinsi.backend.dashboard.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BirdTypeSalesResponse {
    private String jenisBurung;
    private Double persentase;
    private Long jumlah;
    private String tanggalMulai;
    private String tanggalAkhir;
    private List<TrendDataResponse> dailyBreakdown;
}
