package com.propinsi.backend.dashboard.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrendDataResponse {
    private String tanggal;
    private Long jumlah;
}
