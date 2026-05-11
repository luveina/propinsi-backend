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
     // Isinya: "WAITING", "KONCER", atau "FINISH"
    private String nextStep; 
    
    // List semua burung (untuk klasemen umum)
    private List<GantanganRankingResponse> rankings;
    
    // List spesifik burung yang seri/masuk koncer (Hanya ada isinya kalau nextStep == "KONCER")
    private List<GantanganRankingResponse> koncerQualifiers; 
}