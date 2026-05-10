package com.propinsi.backend.penjurian.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KoncerStatusResponse {
    private boolean hasSubmitted; 
    private long totalJuriSubmitted;
    private boolean isKoncerFinished; 
}