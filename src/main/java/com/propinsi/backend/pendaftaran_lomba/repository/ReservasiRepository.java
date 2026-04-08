package com.propinsi.backend.pendaftaran_lomba.repository;

import com.propinsi.backend.pendaftaran_lomba.model.Reservasi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReservasiRepository extends JpaRepository<Reservasi, UUID> {
    
}
