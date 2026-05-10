package com.propinsi.backend.pendaftaran_lomba.service;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.repository.GantanganRepository;
import com.propinsi.backend.mengelola_lomba.repository.LombaRepository;
import com.propinsi.backend.pendaftaran_lomba.model.Reservasi;
import com.propinsi.backend.pendaftaran_lomba.model.StatusReservasi;
import com.propinsi.backend.pendaftaran_lomba.repository.ReservasiRepository;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.ParticipantResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private LombaRepository lombaRepository;

    @Autowired
    private GantanganRepository gantanganRepository;

    @Autowired
    private ReservasiRepository reservasiRepository;

    @Override
    public List<ParticipantResponse> getParticipants(UUID eventId, String classId, String keyword, String attendanceStatus) {
        // Validate event exists
        Lomba event = lombaRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Get all gantangan for this event and class
        List<Gantangan> gantangans = gantanganRepository.findByLombaIdOrderByNomorGantanganAsc(eventId);

        // Filter by class if provided
        if (classId != null && !classId.isEmpty()) {
            gantangans = gantangans.stream()
                    .filter(g -> event.getKelas().equalsIgnoreCase(classId))
                    .collect(Collectors.toList());
        }

        // Filter by attendance status if provided
        if (attendanceStatus != null && !attendanceStatus.isEmpty()) {
            if ("present".equalsIgnoreCase(attendanceStatus)) {
                gantangans = gantangans.stream()
                        .filter(g -> Boolean.TRUE.equals(g.getIsPresent()))
                        .collect(Collectors.toList());
            } else if ("absent".equalsIgnoreCase(attendanceStatus)) {
                gantangans = gantangans.stream()
                        .filter(g -> Boolean.FALSE.equals(g.getIsPresent()))
                        .collect(Collectors.toList());
            }
        }

        // Convert to response and apply keyword filter
        List<ParticipantResponse> responses = gantangans.stream()
                .map(this::convertToParticipantResponse)
                .collect(Collectors.toList());

        // Apply keyword search if provided
        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            responses = responses.stream()
                    .filter(p -> p.getParticipantName().toLowerCase().contains(lowerKeyword) ||
                               p.getGantanganNo().toString().contains(keyword))
                    .collect(Collectors.toList());
        }

        return responses;
    }

    @Override
    public ParticipantResponse checkIn(UUID gantanganId, Boolean isPresent) {
        // Get gantangan
        Gantangan gantangan = gantanganRepository.findById(gantanganId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        // Verify participant has PAID status
        Reservasi reservasi = reservasiRepository.findByGantangan(gantangan)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (!StatusReservasi.PAID.equals(reservasi.getStatus())) {
            throw new IllegalStateException("Participant must have PAID status to check in");
        }

        // Update attendance status
        gantangan.setIsPresent(isPresent);
        gantanganRepository.save(gantangan);

        return convertToParticipantResponse(gantangan);
    }

    private ParticipantResponse convertToParticipantResponse(Gantangan gantangan) {
        ParticipantResponse response = new ParticipantResponse();
        response.setGantanganNo(gantangan.getNomorGantangan());
        response.setParticipantId(gantangan.getId());
        
        if (gantangan.getPeserta() != null) {
            response.setParticipantName(gantangan.getPeserta().getFullName());
            response.setPhoneNumber(gantangan.getPeserta().getPhoneNumber());
        }
        
        if (gantangan.getLomba() != null) {
            response.setBirdType(gantangan.getLomba().getJenisBurung().toString());
            response.setClassValue(gantangan.getLomba().getKelas());
        }
        
        response.setIsPresent(gantangan.getIsPresent());
        return response;
    }
}
