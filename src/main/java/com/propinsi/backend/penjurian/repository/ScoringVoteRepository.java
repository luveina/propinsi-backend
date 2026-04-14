package com.propinsi.backend.penjurian.repository;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.penjurian.model.ScoringVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoringVoteRepository extends JpaRepository<ScoringVote, UUID> {
    Optional<ScoringVote> findByJuriIdAndLombaIdAndBlokId(Long juriId, UUID lombaId, Integer blokId);
    boolean existsByJuriIdAndLombaIdAndBlokId(Long juriId, UUID lombaId, Integer blokId);
    List<ScoringVote> findByJuriIdAndLombaId(Long juriId, UUID lombaId);
    List<ScoringVote> findBySelectedGantangansContains(Gantangan gantangan);
}
