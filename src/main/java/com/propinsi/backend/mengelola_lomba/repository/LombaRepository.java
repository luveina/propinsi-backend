package com.propinsi.backend.mengelola_lomba.repository;

import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LombaRepository extends JpaRepository<Lomba, UUID>, JpaSpecificationExecutor<Lomba> {

    // Bypasses @Where(status != 'DIBATALKAN') — untuk admin & koordinator lomba
    @Query(value = "SELECT * FROM lomba", nativeQuery = true)
    List<Lomba> findAllIncludingDeleted();
}