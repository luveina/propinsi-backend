package com.propinsi.backend.penjurian.restdto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringActionRequest {
    @NotNull(message = "gantanganId wajib diisi")
    private UUID gantanganId;
    
    private Integer blokId; // From AC, though not strictly needed by service
}
