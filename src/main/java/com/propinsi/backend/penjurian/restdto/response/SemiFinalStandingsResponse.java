package com.propinsi.backend.penjurian.restdto.response;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder
public class SemiFinalStandingsResponse {
    private UUID lombaId;
    private String namaLomba;
    private int juriSubmitted; // Jumlah juri yang sudah kelar 4 blok
    private int totalJuri;     // Target 4
    private String nextStep;    // "WAITING", "KONCER", atau "FINISH"
    private List<GantanganRankingResponse> rankings;
}