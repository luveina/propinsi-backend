package com.propinsi.backend.penjurian.restdto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class KoncerVoteSubmitRequest {
    @NotEmpty(message = "Vote tidak boleh kosong")
    @Valid
    private List<KoncerVoteItemRequest> votes;
}