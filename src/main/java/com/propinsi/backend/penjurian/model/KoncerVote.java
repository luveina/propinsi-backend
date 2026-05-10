package com.propinsi.backend.penjurian.model;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class KoncerVote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lomba_id", nullable = false)
    private Lomba lomba;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juri_id", nullable = false)
    private User juri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gantangan_id", nullable = false)
    private Gantangan gantangan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KoncerPoinType poin;

    @CreationTimestamp
    private LocalDateTime createdAt;
}