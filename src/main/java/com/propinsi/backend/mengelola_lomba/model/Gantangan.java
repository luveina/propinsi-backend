package com.propinsi.backend.mengelola_lomba.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.propinsi.backend.model.User;
import com.propinsi.backend.pendaftaran_lomba.model.StatusGantangan;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Gantangan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Min(1) @Max(24) //belum disesuaikan nomornya dgn yg ada di lapangan
    private Integer nomorGantangan;

    @NotBlank
    private Integer blok;

    @Enumerated(EnumType.STRING)
    private StatusGantangan status = StatusGantangan.AVAILABLE;

    private Boolean isAvailable = true;

    private LocalDateTime bookedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peserta_id")
    private User peserta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lomba_id")
    @JsonBackReference
    private Lomba lomba;

}