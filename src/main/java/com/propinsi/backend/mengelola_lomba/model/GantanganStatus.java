package com.propinsi.backend.mengelola_lomba.model;

public enum GantanganStatus {
    ACTIVE,
    DISQUALIFIED,
    BOOKED,
    AVAILABLE;

    public GantanganStatus normalized() {
        return this; // Tidak memaksakan semuanya menjadi ACTIVE lagi
    }
}
