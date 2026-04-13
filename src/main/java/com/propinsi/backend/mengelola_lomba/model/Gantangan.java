package com.propinsi.backend.mengelola_lomba.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.propinsi.backend.model.User;

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

    private Boolean isAvailable = true;

    @Enumerated(EnumType.STRING)
    private GantanganStatus status = GantanganStatus.ACTIVE;

    @Min(0) @Max(3)
    private Integer warningCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peserta_id")
    private User peserta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lomba_id")
    @JsonBackReference
    private Lomba lomba;

}