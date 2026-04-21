package com.propinsi.backend.pendaftaran_lomba.restdto.response;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReservasiResponse {
    private UUID id;
    private String namaPeserta;
    private String username;
    private String namaLomba;
    private Integer nomorGantangan;
    private Double nominal;
    private String urlBukti;
    private String status;
    private LocalDateTime waktuReservasi;
    private String keteranganTolak;
    private Integer rejectionCount;
}