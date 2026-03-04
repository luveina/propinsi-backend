package com.propinsi.backend.mengelola_lomba.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonBackReference;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lomba_id")
    @JsonBackReference
    private Lomba lomba;

}