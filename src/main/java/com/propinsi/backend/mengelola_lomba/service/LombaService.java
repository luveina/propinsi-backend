package com.propinsi.backend.mengelola_lomba.service;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import com.propinsi.backend.mengelola_lomba.repository.GantanganRepository;
import com.propinsi.backend.mengelola_lomba.repository.LombaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.propinsi.backend.mengelola_lomba.restdto.response.*;
import com.propinsi.backend.mengelola_lomba.restdto.request.*;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LombaService {

    @Autowired
    private LombaRepository lombaRepository;
    @Autowired
    private GantanganRepository gantanganRepository;

    public LombaResponse createLomba(LombaRequest request) {
        // 1. Map DTO Request ke Entity Lomba
        Lomba lomba = new Lomba();
        lomba.setNamaLomba(request.getNamaLomba());
        lomba.setLokasi(request.getLokasi());
        lomba.setWaktuTanggal(request.getWaktuTanggal());
        lomba.setJenisBurung(request.getJenisBurung());
        lomba.setKelas(request.getKelas());
        lomba.setHargaTiket(request.getHargaTiket());
        lomba.setHadiah(request.getHadiah());
        lomba.setJumlahJuri(request.getJumlahJuri());
        lomba.setContactPerson(request.getContactPerson());
        lomba.setStatus(StatusLomba.BELUM_DIMULAI);

        Lomba savedLomba = lombaRepository.save(lomba);

        // 2. Buat 24 Gantangan
        List<Gantangan> gantangans = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            Gantangan g = new Gantangan();
            g.setNomorGantangan(i);
            g.setIsAvailable(true);
            g.setLomba(savedLomba);
            gantangans.add(g);
        }
        gantanganRepository.saveAll(gantangans);
        savedLomba.setListGantangan(gantangans);

        // 3. Map Entity ke Response DTO
        return mapToResponse(savedLomba);
    }

    public LombaResponse getLombaById(UUID id) {
        Lomba lomba = lombaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lomba tidak ditemukan"));
        return mapToResponse(lomba);
    }

    // Helper method untuk mengubah Entity ke Response DTO
    private LombaResponse mapToResponse(Lomba lomba) {
        LombaResponse response = new LombaResponse();
        response.setId(lomba.getId());
        response.setNamaLomba(lomba.getNamaLomba());
        response.setLokasi(lomba.getLokasi());
        response.setWaktuTanggal(lomba.getWaktuTanggal());
        response.setJenisBurung(lomba.getJenisBurung());
        response.setKelas(lomba.getKelas());
        response.setHargaTiket(lomba.getHargaTiket());
        response.setHadiah(lomba.getHadiah());
        response.setJumlahJuri(lomba.getJumlahJuri());
        response.setContactPerson(lomba.getContactPerson());
        response.setStatus(lomba.getStatus());

        if (lomba.getListGantangan() != null) {
            List<GantanganResponse> gResponses = lomba.getListGantangan().stream().map(g -> {
                GantanganResponse gr = new GantanganResponse();
                gr.setId(g.getId());
                gr.setNomorGantangan(g.getNomorGantangan());
                gr.setIsAvailable(g.getIsAvailable());
                return gr;
            }).toList();
            response.setListGantangan(gResponses);
        }
        return response;
    }
}