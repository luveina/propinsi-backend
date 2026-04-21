package com.propinsi.backend.pendaftaran_lomba.restdto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketResponse {

    private UUID id;

    // Info lomba
    private String namaLomba;
    private String tanggal;       // format: "Mar 28, 2026 - 11.00 WIB"
    private String lokasi;
    private String jenisBurung;
    private String kelas;

    /**
     * Status yang dikirim ke FE:
     * "Paid" | "Unpaid" | "Menunggu Konfirmasi" | "Invalid"
     *
     * Mapping dari StatusReservasi:
     *   PAID                  → "Paid"
     *   BOOKED                → "Unpaid"
     *   MENUNGGU_KONFIRMASI   → "Menunggu Konfirmasi"
     *   REJECTED              → "Invalid"
     *   EXPIRED               → "Invalid" (dengan keteranganTolak khusus)
     */
    private String status;

    /**
     * Alasan penolakan — hanya diisi jika status = "Invalid".
     * null untuk status lainnya.
     */
    private String keteranganTolak;

    /**
     * true  → peserta boleh upload ulang bukti pembayaran
     * false → reservasi expired (2x ditolak), harus reservasi ulang
     */
    private Boolean canReupload;

    // Dibutuhkan halaman upload agar bisa render nominal dan countdown yang konsisten.
    private Double nominal;
    private LocalDateTime waktuReservasi;

    // Hanya diisi jika status = "Paid"
    private Integer blok;
    private Integer nomorGantangan;
}