package com.propinsi.backend.pendaftaran_lomba.service;

import com.propinsi.backend.pendaftaran_lomba.model.Reservasi;
import com.propinsi.backend.pendaftaran_lomba.model.StatusReservasi;
import com.propinsi.backend.pendaftaran_lomba.repository.ReservasiRepository;
import com.propinsi.backend.pendaftaran_lomba.restdto.request.VerifyRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.ReservasiResponse;
import com.propinsi.backend.mengelola_lomba.repository.GantanganRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservasiServiceImpl {

    @Autowired
    private ReservasiRepository reservasiRepository;
    
    @Autowired
    private GantanganRepository gantanganRepository; // Pastikan ini bisa diakses

    public List<ReservasiResponse> getAllReservasi() {
        return reservasiRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ReservasiResponse verifyPembayaran(UUID id, VerifyRequest request) {
        Reservasi reservasi = reservasiRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Data reservasi tidak ditemukan"));

        if ("PAID".equalsIgnoreCase(request.getStatus())) {
            reservasi.setStatus(StatusReservasi.PAID);
            
            // Jadikan seat permanen terisi sesuai Acceptance Criteria
            var gantangan = reservasi.getGantangan();
            gantangan.setIsAvailable(false);
            gantangan.setPeserta(reservasi.getPeserta());
            gantanganRepository.save(gantangan);
            
        } else if ("REJECTED".equalsIgnoreCase(request.getStatus())) {
            reservasi.setStatus(StatusReservasi.REJECTED);
            // Lepas seat kembali ke Available (Bisa kerjakan nanti sesuai arahanmu)
        }

        return mapToResponse(reservasiRepository.save(reservasi));
    }

    private ReservasiResponse mapToResponse(Reservasi res) {
        ReservasiResponse dto = new ReservasiResponse();
        dto.setId(res.getId());
        dto.setNamaPeserta(res.getPeserta().getFullName());
        dto.setUsername(res.getPeserta().getUsername());
        dto.setNamaLomba(res.getLomba().getNamaLomba());
        dto.setNomorGantangan(res.getGantangan().getNomorGantangan());
        dto.setNominal(res.getNominal());
        dto.setUrlBukti(res.getUrlBuktiPembayaran());
        dto.setStatus(res.getStatus().name());
        dto.setWaktuReservasi(res.getWaktuReservasi());
        return dto;
    }
}