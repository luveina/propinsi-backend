package com.propinsi.backend.mengelola_lomba.restdto.request;

import com.propinsi.backend.mengelola_lomba.model.JenisBurung;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LombaRequest {
    @NotBlank(message = "Nama Lomba wajib diisi")
    private String namaLomba;

    @NotBlank(message = "Lokasi wajib diisi")
    private String lokasi;

    @NotNull(message = "Waktu dan Tanggal wajib diisi")
    @Future(message = "Tanggal tidak boleh di waktu lampau")
    private LocalDateTime waktuTanggal;

    @NotNull(message = "Jenis Burung wajib dipilih")
    private JenisBurung jenisBurung;

    @NotBlank(message = "Kelas wajib diisi")
    private String kelas;

    @NotNull(message = "Harga Tiket wajib diisi")
    @Min(0)
    private Double hargaTiket;

    @NotNull(message = "Hadiah wajib diisi")
    @Size(min = 1, message = "Minimal harus ada 1 hadiah")
    private List<Long> hadiah;

    @NotNull(message = "Jumlah Juri wajib diisi")
    @Min(1)
    private Integer jumlahJuri;

    @NotBlank(message = "Contact Person wajib diisi")
    private String contactPerson;

    private String deskripsi;
}