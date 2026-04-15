package com.propinsi.backend.pendaftaran_lomba.controller;

import com.propinsi.backend.pendaftaran_lomba.restdto.request.BookingRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.request.VerifyRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.BookingResponse;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.DenahResponse;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.ReservasiResponse;
import com.propinsi.backend.pendaftaran_lomba.service.ReservasiService;
import com.propinsi.backend.pendaftaran_lomba.service.ReservasiServiceImpl;
import com.propinsi.backend.restdto.response.BaseResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservasi")
public class ReservasiController {

    @Autowired
    private ReservasiServiceImpl reservasiService;

    @GetMapping("/denah/{lombaId}")
    public ResponseEntity<BaseResponse<List<DenahResponse>>> getDenah(@PathVariable UUID lombaId) {
        List<DenahResponse> data = reservasiService.getDenah(lombaId);
        return ResponseEntity.ok(BaseResponse.success(data, "Data denah berhasil diambil"));
    }

    @PreAuthorize("hasRole('PESERTA')")
    @PostMapping("/book-seat")
    public ResponseEntity<BaseResponse<BookingResponse>> bookSeat(@RequestBody BookingRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        BookingResponse response = reservasiService.bookSeat(request, username);
        return ResponseEntity.ok(BaseResponse.success(response, "Booking berhasil, silakan lanjut ke pembayaran"));
    }

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

    @PostMapping(value = "/upload-bukti", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBukti(
            @RequestParam("reservasiId") UUID reservasiId,
            @RequestParam("file") MultipartFile file) {
        
        ReservasiResponse response = reservasiService.uploadBuktiPembayaran(reservasiId, file);
        return ResponseEntity.ok().body(response);
    }
}