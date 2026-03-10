package com.propinsi.backend.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    ADMIN("Admin"),
    JURI("Juri"),
    KOORDINATOR_LOMBA("Koordinator Lomba"),
    KOORDINATOR_PENDAFTARAN("Koordinator Pendaftaran"),
    PESERTA("Peserta");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Parse a string coming from the client (either enum name or human label) into a Role.
     *
     * @param input string value that may equal name() or label (case insensitive)
     * @return matching Role
     * @throws IllegalArgumentException if no match found
     */
    @JsonCreator
    public static Role fromString(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Role tidak boleh kosong");
        }
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(input) || r.label.equalsIgnoreCase(input)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Role tidak valid: " + input);
    }
}