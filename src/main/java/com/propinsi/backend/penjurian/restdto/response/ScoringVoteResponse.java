package com.propinsi.backend.penjurian.restdto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringVoteResponse {
    private String message;
    private Integer blokId;
    private Boolean locked;
}
