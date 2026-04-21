package com.propinsi.backend.mengelola_lomba.restdto.response;

import com.propinsi.backend.mengelola_lomba.model.JenisBurung;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LombaResponse {
    private UUID id;
    private String namaLomba;
    private String lokasi;
    private LocalDateTime waktuTanggal;
    private JenisBurung jenisBurung;
    private String kelas;
    private Double hargaTiket;
    private List<Long> hadiah;
    private Integer jumlahJuara;
    private Integer jumlahJuri;
    private List<UserSummaryResponse> listJuri;
    private String contactPerson;
    private StatusLomba status;
    private List<GantanganResponse> listGantangan;
    private String deskripsi;
}
