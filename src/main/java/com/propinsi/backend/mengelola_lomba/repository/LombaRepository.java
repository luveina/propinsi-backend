package com.propinsi.backend.mengelola_lomba.repository;

import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface LombaRepository extends JpaRepository<Lomba, UUID>, JpaSpecificationExecutor<Lomba> {

    // Bypasses @Where(status != 'DIBATALKAN') — untuk admin & koordinator lomba
    @Query(value = "SELECT * FROM lomba", nativeQuery = true)
    List<Lomba> findAllIncludingDeleted();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lomba l WHERE l.id = :id")
    Optional<Lomba> findByIdForUpdate(@Param("id") UUID id);
}