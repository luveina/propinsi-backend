package com.propinsi.backend.pendaftaran_lomba.restdto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {
    private Boolean isPresent;
}
