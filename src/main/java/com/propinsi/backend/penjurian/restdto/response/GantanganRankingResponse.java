package com.propinsi.backend.penjurian.restdto.response;

import lombok.*;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder
public class GantanganRankingResponse {
    private Integer nomorGantangan;
    private Integer blokId;
    private Long jumlahAjuan;
}