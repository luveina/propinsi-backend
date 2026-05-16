package com.propinsi.backend.mengelola_lomba.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalResultResponse {
    private List<FinalResultGantanganResponse> results;
}
