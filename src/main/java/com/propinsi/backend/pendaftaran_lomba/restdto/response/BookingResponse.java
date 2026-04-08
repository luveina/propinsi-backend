package com.propinsi.backend.pendaftaran_lomba.restdto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    private UUID reservationId;
    private LocalDateTime expiryTime;
}