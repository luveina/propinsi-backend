package com.propinsi.backend.mengelola_lomba.restdto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.propinsi.backend.mengelola_lomba.model.JenisBurung;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LombaDetailResponse {
    private UUID id;
    private String namaLomba;
    private String deskripsi;
    private String lokasi;
    private LocalDateTime waktuTanggal;
    private JenisBurung jenisBurung;
    private String kelas;
    private Double hargaTiket;
    private List<Long> hadiah;
    private Integer jumlahGantangan;
    private Integer jumlahJuri;
    private List<UserSummaryResponse> listJuri;
    private String contactPerson;
    private StatusLomba status;
    private List<GantanganResponse> listGantangan; 

    @JsonProperty("isEditable")
    private boolean isEditable;

    @JsonProperty("isReservable")
    private boolean isReservable;
    
    @JsonProperty("canStartJudging")
    private boolean canStartJudging;
    
    @JsonProperty("canToggleOngoing")
    private boolean canToggleOngoing;
    
    @JsonProperty("isFullBooked")
    private boolean isFullBooked;
    
    @JsonProperty("canViewWinner")
    private boolean canViewWinner;
}