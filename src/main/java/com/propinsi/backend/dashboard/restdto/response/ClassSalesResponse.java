package com.propinsi.backend.dashboard.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ClassSalesResponse {
    private String kelas;
    private Double persentase;
    private Long jumlah;
    private String tanggalMulai;
    private String tanggalAkhir;
    private List<TrendDataResponse> dailyBreakdown;
}
