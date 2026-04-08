package com.propinsi.backend.pendaftaran_lomba.model;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.model.User;
import com.propinsi.backend.pendaftaran_lomba.model.StatusReservasi;

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

    private String urlBuktiPembayaran; // URL foto/gambar bukti

    @Enumerated(EnumType.STRING)
    private StatusReservasi status = StatusReservasi.BOOKED;
}