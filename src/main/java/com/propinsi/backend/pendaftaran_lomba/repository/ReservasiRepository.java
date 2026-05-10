package com.propinsi.backend.pendaftaran_lomba.repository;

import com.propinsi.backend.model.User;
import com.propinsi.backend.pendaftaran_lomba.model.Reservasi;
import com.propinsi.backend.pendaftaran_lomba.model.StatusReservasi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservasiRepository extends JpaRepository<Reservasi, UUID> {

    /**
     * Ambil semua reservasi milik user tertentu,
     * diurutkan dari yang terbaru.
     * Dipakai oleh GET /api/profile/my-tickets
     */
    List<Reservasi> findByPesertaOrderByWaktuReservasiDesc(User peserta);
    List<Reservasi> findByStatus(StatusReservasi status);
    
    boolean existsByLombaIdAndStatusIn(UUID lombaId, List<StatusReservasi> statuses);

    List<Reservasi> findByStatusAndWaktuReservasiBetween(StatusReservasi status, LocalDateTime start, LocalDateTime end);
}