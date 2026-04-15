package com.propinsi.backend.penjurian.model;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
    name = "scoring_vote",
    uniqueConstraints = @UniqueConstraint(columnNames = {"juri_id", "lomba_id", "blok_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juri_id", nullable = false)
    private User juri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lomba_id", nullable = false)
    private Lomba lomba;

    @Column(name = "blok_id", nullable = false)
    private Integer blokId;

    @ManyToMany
    @JoinTable(
        name = "scoring_vote_gantangan",
        joinColumns = @JoinColumn(name = "vote_id"),
        inverseJoinColumns = @JoinColumn(name = "gantangan_id")
    )
    @Builder.Default
    private Set<Gantangan> selectedGantangans = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    @PreUpdate
    protected void touchSubmittedAt() {
        submittedAt = LocalDateTime.now();
    }
}
