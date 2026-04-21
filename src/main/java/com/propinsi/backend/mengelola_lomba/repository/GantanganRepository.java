package com.propinsi.backend.mengelola_lomba.repository;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GantanganRepository extends JpaRepository<Gantangan, UUID> {
	List<Gantangan> findByLombaIdOrderByNomorGantanganAsc(UUID lombaId);
	List<Gantangan> findByIdIn(Collection<UUID> ids);
}