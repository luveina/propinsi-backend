package com.propinsi.backend.pendaftaran_lomba.service;

import java.util.List;
import java.util.UUID;

import com.propinsi.backend.pendaftaran_lomba.restdto.request.BookingRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.request.VerifyRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.BookingResponse;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.DenahResponse;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.ReservasiResponse;

public interface ReservasiService {
    List<ReservasiResponse> getAllReservasi();
    ReservasiResponse verifyPembayaran(UUID id, VerifyRequest request);
    List<DenahResponse> getDenah(UUID lombaId);
    BookingResponse bookSeat(BookingRequest request, String username);

}
