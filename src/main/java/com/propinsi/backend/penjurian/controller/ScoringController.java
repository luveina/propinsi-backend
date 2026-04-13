package com.propinsi.backend.penjurian.controller;

import com.propinsi.backend.model.User;
import com.propinsi.backend.penjurian.restdto.request.ScoringVoteRequest;
import com.propinsi.backend.penjurian.restdto.response.ScoringBlokDetailResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringBlokSummaryResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringGantanganResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringVoteResponse;
import com.propinsi.backend.penjurian.service.ScoringService;
import com.propinsi.backend.restdto.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scoring")
@PreAuthorize("hasRole('JURI')")
public class ScoringController {

    @Autowired
    private ScoringService scoringService;

    @GetMapping("/blok")
    public ResponseEntity<BaseResponse<List<ScoringBlokSummaryResponse>>> getBlokSummary(
            @AuthenticationPrincipal User juriLogin,
            @RequestParam UUID lombaId) {
        return ResponseEntity.ok(BaseResponse.success(scoringService.getBlokSummary(lombaId, juriLogin.getId()), "Daftar blok berhasil diambil"));
    }

    @GetMapping("/blok/{blokId}")
    public ResponseEntity<BaseResponse<ScoringBlokDetailResponse>> getBlokDetail(
            @AuthenticationPrincipal User juriLogin,
            @RequestParam UUID lombaId,
            @PathVariable Integer blokId) {
        return ResponseEntity.ok(BaseResponse.success(scoringService.getBlokDetail(lombaId, juriLogin.getId(), blokId), "Data blok berhasil diambil"));
    }

    @PostMapping("/vote")
    public ResponseEntity<BaseResponse<ScoringVoteResponse>> submitVote(
            @AuthenticationPrincipal User juriLogin,
            @RequestParam UUID lombaId,
            @Valid @RequestBody ScoringVoteRequest request) {
        return ResponseEntity.ok(BaseResponse.success(scoringService.submitVote(lombaId, juriLogin.getId(), request), "Vote berhasil disimpan"));
    }

    @PostMapping("/warning/{gantanganId}")
    public ResponseEntity<BaseResponse<ScoringGantanganResponse>> addWarning(
            @AuthenticationPrincipal User juriLogin,
            @RequestParam UUID lombaId,
            @PathVariable UUID gantanganId) {
        return ResponseEntity.ok(BaseResponse.success(scoringService.addWarning(lombaId, juriLogin.getId(), gantanganId), "Peringatan berhasil ditambahkan"));
    }

    @PostMapping("/disqualify/{gantanganId}")
    public ResponseEntity<BaseResponse<ScoringGantanganResponse>> disqualify(
            @AuthenticationPrincipal User juriLogin,
            @RequestParam UUID lombaId,
            @PathVariable UUID gantanganId) {
        return ResponseEntity.ok(BaseResponse.success(scoringService.disqualify(lombaId, juriLogin.getId(), gantanganId), "Gantangan berhasil didiskualifikasi"));
    }
}
