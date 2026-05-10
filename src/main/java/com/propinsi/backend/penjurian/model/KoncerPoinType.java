package com.propinsi.backend.penjurian.model;

public enum KoncerPoinType {
    A(100),
    B(40);

    private final int value;

    KoncerPoinType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
