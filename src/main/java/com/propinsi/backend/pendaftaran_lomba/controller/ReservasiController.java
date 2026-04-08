package com.propinsi.backend.pendaftaran_lomba.controller;

import com.propinsi.backend.pendaftaran_lomba.restdto.request.VerifyRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.ReservasiResponse;
import com.propinsi.backend.pendaftaran_lomba.service.ReservasiServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservasi")
public class ReservasiController {

    @Autowired
    private ReservasiServiceImpl reservasiService;

    // @PreAuthorize("hasAuthority('KOORDINATOR_PENDAFTARAN')")
    @GetMapping("/all")
    public ResponseEntity<List<ReservasiResponse>> getAll() {
        return ResponseEntity.ok(reservasiService.getAllReservasi());
    }

    // @PreAuthorize("hasAuthority('KOORDINATOR_PENDAFTARAN')")
    @PatchMapping("/verify/{id}")
    public ResponseEntity<ReservasiResponse> verify(@PathVariable UUID id, @RequestBody VerifyRequest request) {
        return ResponseEntity.ok(reservasiService.verifyPembayaran(id, request));
    }
}