package com.propinsi.backend.penjurian.restdto.response;

import java.util.UUID;
import lombok.*;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder
public class GantanganRankingResponse {
    private UUID gantanganId;
    private Integer nomorGantangan;
    private Integer blokId;
    private Long jumlahAjuan;
}