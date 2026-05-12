package com.propinsi.backend.penjurian.repository;

import com.propinsi.backend.penjurian.model.KoncerVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KoncerVoteRepository extends JpaRepository<KoncerVote, UUID> {
    boolean existsByLombaIdAndJuriId(UUID lombaId, Long juriId);
    
    @Query("SELECT COUNT(DISTINCT kv.juri.id) FROM KoncerVote kv WHERE kv.lomba.id = :lombaId")
    long countDistinctJuriByLombaId(@Param("lombaId") UUID lombaId);
    
    List<KoncerVote> findByLombaId(UUID lombaId);
    
    List<KoncerVote> findByLombaIdAndJuriId(UUID lombaId, Long juriId);
}