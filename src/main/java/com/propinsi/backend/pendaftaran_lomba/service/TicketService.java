package com.propinsi.backend.pendaftaran_lomba.service;

import com.propinsi.backend.model.User;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.TicketResponse;

import java.util.List;

public interface TicketService {

    /**
     * Ambil semua tiket milik user yang sedang login.
     * ID Validation: hanya reservasi milik currentUser yang dikembalikan.
     */
    List<TicketResponse> getMyTickets(User currentUser);
}