package com.propinsi.backend.penjurian.restdto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringBlokSummaryResponse {
    private Integer blokId;
    private String blokLabel;
    private Boolean locked;
}
