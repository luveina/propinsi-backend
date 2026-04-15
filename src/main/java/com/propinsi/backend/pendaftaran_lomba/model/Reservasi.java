package com.propinsi.backend.pendaftaran_lomba.model;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.model.User;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Reservasi {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User peserta;

    @ManyToOne
    @JoinColumn(name = "lomba_id", nullable = false)
    private Lomba lomba;

    @ManyToOne
    @JoinColumn(name = "gantangan_id", nullable = false)
    private Gantangan gantangan;

    private LocalDateTime waktuReservasi;

    private Double nominal;

    private String urlBuktiPembayaran;

    @Enumerated(EnumType.STRING)
    private StatusReservasi status = StatusReservasi.BOOKED;

    @Column(columnDefinition = "TEXT")
    private String keteranganTolak;

    @Column(nullable = false)
    private Integer rejectionCount = 0;
}