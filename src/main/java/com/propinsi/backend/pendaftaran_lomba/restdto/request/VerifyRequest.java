package com.propinsi.backend.pendaftaran_lomba.restdto.request;
import lombok.Data;

@Data
public class VerifyRequest {
    private String status; // "PAID" atau "REJECTED"
}