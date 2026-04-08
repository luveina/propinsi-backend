package com.propinsi.backend.mengelola_lomba.service;

import com.propinsi.backend.mengelola_lomba.restdto.request.AssignJuriRequest;
import com.propinsi.backend.mengelola_lomba.restdto.request.LombaRequest;
import com.propinsi.backend.mengelola_lomba.restdto.response.LombaResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.UserSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface LombaService {
    LombaResponse createLomba(LombaRequest request);
    LombaResponse getLombaById(UUID id);
    List<LombaResponse> getAllLomba();
    List<LombaResponse> getAllLomba(String jenisBurung, String status, String sortBy, String sortDir, boolean includeAll);
    LombaResponse updateLomba(UUID id, LombaRequest request);
    LombaResponse assignJuriToLomba(UUID lombaId, AssignJuriRequest request);
    LombaResponse removeJuriFromLomba(UUID lombaId, Long juriId);
    List<UserSummaryResponse> getAvailableJuri();
    void deleteLomba(UUID id);
}
