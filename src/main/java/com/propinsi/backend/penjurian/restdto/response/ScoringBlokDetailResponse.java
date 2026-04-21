package com.propinsi.backend.penjurian.restdto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringBlokDetailResponse {
    private Integer blokId;
    private String blokLabel;
    private UUID lombaId;
    private String namaLomba;
    private Boolean locked;

    @Builder.Default
    private List<ScoringGantanganResponse> gantangan = new ArrayList<>();
}
