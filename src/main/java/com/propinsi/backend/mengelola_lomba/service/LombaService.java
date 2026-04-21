package com.propinsi.backend.mengelola_lomba.service;

import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import com.propinsi.backend.mengelola_lomba.restdto.request.AssignJuriRequest;
import com.propinsi.backend.mengelola_lomba.restdto.request.LombaRequest;
import com.propinsi.backend.mengelola_lomba.restdto.response.LombaDetailResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.LombaResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.UserSummaryResponse;
import com.propinsi.backend.model.User;

import java.util.List;
import java.util.UUID;

public interface LombaService {
    LombaResponse createLomba(LombaRequest request);
    LombaResponse getLombaById(UUID id);
    List<LombaResponse> getAllLomba();
    List<LombaResponse> getAllLomba(String jenisBurung, String status, String sortBy, String sortDir, boolean includeAll);
    List<LombaResponse> getAllLomba(String jenisBurung, String status, String sortBy, String sortDir, String nama, boolean includeAll);
    List<LombaResponse> getLombaByJuri(Long juriId, String jenisBurung, String status, String sortBy, String sortDir, String nama);
    LombaResponse updateLomba(UUID id, LombaRequest request);
    LombaResponse assignJuriToLomba(UUID lombaId, AssignJuriRequest request);
    LombaResponse removeJuriFromLomba(UUID lombaId, Long juriId);
    List<UserSummaryResponse> getAvailableJuri();
    void deleteLomba(UUID id);
    LombaDetailResponse getLombaDetail(UUID id, User currentUser);
    void updateStatus(UUID id, StatusLomba newStatus);
}
