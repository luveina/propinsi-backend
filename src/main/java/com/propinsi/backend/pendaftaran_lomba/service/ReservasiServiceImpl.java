package com.propinsi.backend.pendaftaran_lomba.service;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.repository.LombaRepository;
import com.propinsi.backend.model.User;
import com.propinsi.backend.pendaftaran_lomba.model.StatusGantangan;
import com.propinsi.backend.pendaftaran_lomba.repository.ReservasiRepository;
import com.propinsi.backend.pendaftaran_lomba.restdto.request.BookingRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.BookingResponse;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.DenahResponse;
import com.propinsi.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservasiServiceImpl implements ReservasiService {

    @Autowired private ReservasiRepository reservasiRepository;
    @Autowired private LombaRepository lombaRepository;
    @Autowired private UserRepository userRepository;

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
        Gantangan gantangan = reservasiRepository.findByLombaIdAndNomorWithLock(request.getLombaId(), request.getNomorGantangan())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gantangan tidak ditemukan"));

        if (gantangan.getStatus() != StatusGantangan.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nomor gantangan sudah dipesan");
        }

        gantangan.setStatus(StatusGantangan.BOOKED);
        gantangan.setPeserta(peserta);
        gantangan.setBookedAt(LocalDateTime.now());
        reservasiRepository.save(gantangan);

        return BookingResponse.builder()
                .reservationId(gantangan.getId())
                .expiryTime(gantangan.getBookedAt().plusHours(2)) // Expiry 2 jam
                .build();
    }
}