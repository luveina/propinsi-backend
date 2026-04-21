package com.propinsi.backend.penjurian.restdto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringVoteRequest {
    @NotNull(message = "blok_id wajib diisi")
    @Min(value = 1, message = "blok_id minimal 1")
    @Max(value = 4, message = "blok_id maksimal 4")
    private Integer blokId;

    @Builder.Default
    private List<UUID> gantanganIds = new ArrayList<>();
}
