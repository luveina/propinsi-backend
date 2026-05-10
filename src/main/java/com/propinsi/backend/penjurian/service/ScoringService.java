package com.propinsi.backend.penjurian.service;

import com.propinsi.backend.penjurian.restdto.request.KoncerVoteSubmitRequest;
import com.propinsi.backend.penjurian.restdto.response.KoncerStatusResponse;
import com.propinsi.backend.penjurian.restdto.request.ScoringVoteRequest;
import com.propinsi.backend.penjurian.restdto.response.ScoringBlokDetailResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringBlokSummaryResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringGantanganResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringVoteResponse;

import java.util.List;
import java.util.UUID;

public interface ScoringService {
    List<ScoringBlokSummaryResponse> getBlokSummary(UUID lombaId, Long juriId);
    ScoringBlokDetailResponse getBlokDetail(UUID lombaId, Long juriId, Integer blokId);
    ScoringVoteResponse submitVote(UUID lombaId, Long juriId, ScoringVoteRequest request);
    ScoringGantanganResponse addWarning(UUID lombaId, Long juriId, UUID gantanganId);
    ScoringGantanganResponse disqualify(UUID lombaId, Long juriId, UUID gantanganId);
    
    // Koncer methods
    KoncerStatusResponse getKoncerStatus(UUID lombaId, Long juriId);
    void submitKoncer(UUID lombaId, Long juriId, KoncerVoteSubmitRequest request);
}
