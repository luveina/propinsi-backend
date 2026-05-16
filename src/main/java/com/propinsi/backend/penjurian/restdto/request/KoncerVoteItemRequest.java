package com.propinsi.backend.penjurian.restdto.request;

import com.propinsi.backend.penjurian.model.KoncerPoinType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class KoncerVoteItemRequest {
    @NotNull(message = "ID Gantangan wajib diisi")
    private UUID gantanganId;

    @NotNull(message = "Poin (A/B) wajib diisi")
    private KoncerPoinType poin;
}