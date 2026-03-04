package com.propinsi.backend.mengelola_lomba.service;

import com.propinsi.backend.mengelola_lomba.restdto.request.LombaRequest;
import com.propinsi.backend.mengelola_lomba.restdto.response.LombaResponse;

import java.util.List;
import java.util.UUID;

public interface LombaService {
    LombaResponse createLomba(LombaRequest request);
    LombaResponse getLombaById(UUID id);
    List<LombaResponse> getAllLomba();
}