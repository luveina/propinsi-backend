package com.propinsi.backend.mengelola_lomba.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalResultResponse {
    private List<FinalResultGantanganResponse> results;
}
