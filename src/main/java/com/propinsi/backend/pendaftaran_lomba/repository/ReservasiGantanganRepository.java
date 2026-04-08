package com.propinsi.backend.pendaftaran_lomba.repository; // Sesuaikan package

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface ReservasiGantanganRepository extends JpaRepository<Gantangan, UUID> {
    
    // Pindahkan method ini ke sini!
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Gantangan g WHERE g.lomba.id = :lombaId AND g.nomorGantangan = :nomor")
    Optional<Gantangan> findByLombaIdAndNomorWithLock(@Param("lombaId") UUID lombaId, @Param("nomor") Integer nomor);
}