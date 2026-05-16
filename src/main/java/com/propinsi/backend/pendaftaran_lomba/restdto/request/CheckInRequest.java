package com.propinsi.backend.pendaftaran_lomba.restdto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {
    @JsonProperty("is_present")
    private Boolean isPresent;
}
