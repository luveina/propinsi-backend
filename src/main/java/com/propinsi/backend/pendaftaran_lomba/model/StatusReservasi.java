package com.propinsi.backend.pendaftaran_lomba.model;

public enum StatusReservasi {
    AVAILABLE,            // Belum dipesan (opsional jika ditaruh di gantangan)
    BOOKED,               // Sedang dipesan (menunggu upload bukti, timer 2 jam)
    MENUNGGU_KONFIRMASI,  // Sudah upload bukti, menunggu Koor Pendaftaran
    PAID,                 // DITERIMA (Lunas)
    REJECTED,             // Ditolak
    EXPIRED               // Waktu habis
}