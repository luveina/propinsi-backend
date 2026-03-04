package com.propinsi.backend.mengelola_lomba.restdto.response;

import com.propinsi.backend.mengelola_lomba.model.JenisBurung;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class LombaResponse {
    private UUID id;
    private String namaLomba;
    private String lokasi;
    private LocalDateTime waktuTanggal;
    private JenisBurung jenisBurung;
    private String kelas;
    private Double hargaTiket;
    private String hadiah;
    private Integer jumlahJuri;
    private String contactPerson;
    private StatusLomba status;
    private List<GantanganResponse> listGantangan;
}