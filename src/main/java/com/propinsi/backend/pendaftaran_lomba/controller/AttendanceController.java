package com.propinsi.backend.pendaftaran_lomba.controller;

import com.propinsi.backend.pendaftaran_lomba.restdto.request.CheckInRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.ParticipantResponse;
import com.propinsi.backend.pendaftaran_lomba.service.AttendanceService;
import com.propinsi.backend.restdto.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PreAuthorize("hasRole('KOORDINATOR_PENDAFTARAN')")
    @GetMapping("/{eventId}/participants")
    public ResponseEntity<BaseResponse<List<ParticipantResponse>>> getParticipants(
            @PathVariable UUID eventId,
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String attendance_status) {
        List<ParticipantResponse> participants = attendanceService.getParticipants(eventId, classId, keyword, attendance_status);
        return ResponseEntity.ok(BaseResponse.success(participants, "Data peserta berhasil diambil"));
    }

    @PreAuthorize("hasRole('KOORDINATOR_PENDAFTARAN')")
    @PutMapping("/participants/{participantId}/check-in")
    public ResponseEntity<BaseResponse<ParticipantResponse>> checkIn(
            @PathVariable UUID participantId,
            @Valid @RequestBody CheckInRequest request) {
        ParticipantResponse response = attendanceService.checkIn(participantId, request.getIsPresent());
        return ResponseEntity.ok(BaseResponse.success(response, "Status kehadiran berhasil diperbarui"));
    }
}
