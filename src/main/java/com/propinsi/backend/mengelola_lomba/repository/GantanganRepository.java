package com.propinsi.backend.mengelola_lomba.repository;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GantanganRepository extends JpaRepository<Gantangan, Long> {
}