package com.propinsi.backend.pendaftaran_lomba.service;

import com.propinsi.backend.pendaftaran_lomba.model.Reservasi;
import com.propinsi.backend.pendaftaran_lomba.model.StatusReservasi;
import com.propinsi.backend.pendaftaran_lomba.repository.ReservasiGantanganRepository;
import com.propinsi.backend.pendaftaran_lomba.repository.ReservasiRepository;
import com.propinsi.backend.pendaftaran_lomba.restdto.request.BookingRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.request.VerifyRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.BookingResponse;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.DenahResponse;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.ReservasiResponse;
import com.propinsi.backend.repository.UserRepository;
import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.GantanganStatus;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.repository.GantanganRepository;
import com.propinsi.backend.mengelola_lomba.repository.LombaRepository;
import com.propinsi.backend.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservasiServiceImpl implements ReservasiService  {
    
    @Autowired
    private ReservasiRepository reservasiRepository;

    @Autowired 
    private LombaRepository lombaRepository;

    @Autowired 
    private UserRepository userRepository;
    
    @Autowired
    private GantanganRepository gantanganRepository;

    @Autowired
    private ReservasiGantanganRepository reservasiGantanganRepository;

    @Override
    public List<DenahResponse> getDenah(UUID lombaId) {
        Lomba lomba = lombaRepository.findById(lombaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lomba tidak ditemukan"));

        return lomba.getListGantangan().stream()
                .map(g -> new DenahResponse(g.getNomorGantangan(), g.getStatus().name()))
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse bookSeat(BookingRequest request, String username) {
        User peserta = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        // Mengambil data dengan Lock (Kriteria 409 Conflict)
        Gantangan gantangan = reservasiGantanganRepository.findByLombaIdAndNomorWithLock(request.getLombaId(), request.getNomorGantangan())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gantangan tidak ditemukan"));

        if (gantangan.getStatus() != GantanganStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nomor gantangan sudah dipesan");
        }

        gantangan.setStatus(GantanganStatus.BOOKED);
        gantangan.setPeserta(peserta);
        gantangan.setBookedAt(LocalDateTime.now());
        reservasiGantanganRepository.save(gantangan);

        return BookingResponse.builder()
                .reservationId(gantangan.getId())
                .build();
    }

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