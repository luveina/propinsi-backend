package com.propinsi.backend.pendaftaran_lomba.scheduler;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.pendaftaran_lomba.model.StatusGantangan;
import com.propinsi.backend.pendaftaran_lomba.repository.ReservasiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservasiCleanupScheduler {

    @Autowired
    private ReservasiRepository reservasiRepository;

    @Scheduled(fixedRate = 60000) // Jalan tiap 1 menit
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime limitTime = LocalDateTime.now().minusHours(2);
        
        List<Gantangan> expired = reservasiRepository.findAll().stream()
                .filter(g -> g.getStatus() == StatusGantangan.BOOKED && g.getBookedAt() != null && g.getBookedAt().isBefore(limitTime))
                .collect(Collectors.toList());

        for (Gantangan g : expired) {
            g.setStatus(StatusGantangan.AVAILABLE);
            g.setPeserta(null);
            g.setBookedAt(null);
        }
        reservasiRepository.saveAll(expired);
    }
}