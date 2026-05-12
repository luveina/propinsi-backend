package com.propinsi.backend.penjurian.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KoncerStatusResponse {
    private boolean hasSubmitted; 
    private long totalJuriSubmitted;
    private boolean isKoncerFinished; 
    private Map<String, String> userVotes;
}