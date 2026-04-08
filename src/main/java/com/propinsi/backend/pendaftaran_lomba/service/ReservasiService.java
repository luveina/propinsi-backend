package com.propinsi.backend.pendaftaran_lomba.service;

import com.propinsi.backend.pendaftaran_lomba.restdto.request.BookingRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.BookingResponse;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.DenahResponse;

import java.util.List;
import java.util.UUID;

public interface ReservasiService {
    List<DenahResponse> getDenah(UUID lombaId);
    BookingResponse bookSeat(BookingRequest request, String username);
}