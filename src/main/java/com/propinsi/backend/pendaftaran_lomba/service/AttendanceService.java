package com.propinsi.backend.pendaftaran_lomba.service;

import com.propinsi.backend.pendaftaran_lomba.restdto.response.ParticipantResponse;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    List<ParticipantResponse> getParticipants(UUID eventId, String classId, String keyword, String attendanceStatus);
    ParticipantResponse checkIn(UUID participantId, Boolean isPresent);
}
