package com.propinsi.backend.pendaftaran_lomba.controller;

import com.propinsi.backend.model.User;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.TicketResponse;
import com.propinsi.backend.pendaftaran_lomba.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * GET /api/profile/my-tickets
     *
     * Mengembalikan semua reservasi milik user yang sedang login.
     * ID Validation otomatis via @AuthenticationPrincipal —
     * user tidak bisa melihat tiket milik user lain.
     *
     * Role: PESERTA
     */
    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketResponse>> getMyTickets(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ticketService.getMyTickets(currentUser));
    }
}