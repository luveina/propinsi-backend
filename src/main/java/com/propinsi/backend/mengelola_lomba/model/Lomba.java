package com.propinsi.backend.mengelola_lomba.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Lomba {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @NotBlank(message = "Nama Lomba wajib diisi")
    private String namaLomba;

    @NotBlank(message = "Lokasi wajib diisi")
    private String lokasi;

    @NotNull(message = "Waktu dan Tanggal wajib diisi")
    @Future(message = "Tanggal tidak boleh di waktu lampau")
    private LocalDateTime waktuTanggal;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Jenis Burung wajib dipilih")
    private JenisBurung jenisBurung;

    @NotBlank(message = "Kelas wajib diisi")
    private String kelas;

    @NotNull(message = "Harga Tiket wajib diisi")
    @Min(0)
    private Double hargaTiket;

    @NotBlank(message = "Hadiah wajib diisi")
    @Column(columnDefinition = "TEXT")
    private String hadiah;

    @NotNull(message = "Jumlah Juri wajib diisi")
    @Min(1)
    private Integer jumlahJuri;

    private String juriAssign; // TEMP String selama blm connect user

    @NotBlank(message = "Contact Person wajib diisi")
    private String contactPerson;

    @Enumerated(EnumType.STRING)
    private StatusLomba status = StatusLomba.BELUM_DIMULAI;

    @OneToMany(mappedBy = "lomba", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Gantangan> listGantangan;
}