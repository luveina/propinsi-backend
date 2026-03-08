package com.propinsi.backend.mengelola_lomba.controller;

import com.propinsi.backend.mengelola_lomba.restdto.request.AssignJuriRequest;
import com.propinsi.backend.mengelola_lomba.restdto.request.LombaRequest;
import com.propinsi.backend.mengelola_lomba.restdto.response.LombaResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.UserSummaryResponse;
import com.propinsi.backend.mengelola_lomba.service.LombaService;
import com.propinsi.backend.restdto.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lomba")
public class LombaController {

    @Autowired
    private LombaService lombaService;

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')") 
    @PostMapping
    public ResponseEntity<LombaResponse> createLomba(@Valid @RequestBody LombaRequest request) {
        LombaResponse response = lombaService.createLomba(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LombaResponse> getLombaById(@PathVariable UUID id) {
        return ResponseEntity.ok(lombaService.getLombaById(id));
    }

    @GetMapping
    public ResponseEntity<List<LombaResponse>> getAllLomba() {
        return ResponseEntity.ok(lombaService.getAllLomba());
    }

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<LombaResponse> updateLomba(
            @PathVariable UUID id, 
            @Valid @RequestBody LombaRequest request) {
        LombaResponse response = lombaService.updateLomba(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')")
    @PostMapping("/{lombaId}/assign-juri")
    public ResponseEntity<LombaResponse> assignJuri(
            @PathVariable UUID lombaId,
            @Valid @RequestBody AssignJuriRequest request
    ) {
        LombaResponse response = lombaService.assignJuriToLomba(lombaId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')")
    @DeleteMapping("/{lombaId}/remove-juri/{juriId}")
    public ResponseEntity<LombaResponse> removeJuri(
            @PathVariable UUID lombaId,
            @PathVariable Long juriId
    ) {
        LombaResponse response = lombaService.removeJuriFromLomba(lombaId, juriId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')")
    @GetMapping("/available-juri")
    public ResponseEntity<List<UserSummaryResponse>> getAvailableJuri() {
        return ResponseEntity.ok(lombaService.getAvailableJuri());
    }

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteLomba(@PathVariable UUID id) {
        lombaService.deleteLomba(id);
        return ResponseEntity.ok(BaseResponse.success(null, "Lomba berhasil dihapus"));
    }
}