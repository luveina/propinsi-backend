package com.propinsi.backend.pendaftaran_lomba.restdto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class BookingRequest {
    private UUID lombaId;
    private Integer nomorGantangan;
}