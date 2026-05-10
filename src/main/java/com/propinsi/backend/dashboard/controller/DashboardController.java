package com.propinsi.backend.dashboard.controller;

import com.propinsi.backend.dashboard.restdto.response.AnalyticsResponse;
import com.propinsi.backend.dashboard.service.DashboardService;
import com.propinsi.backend.restdto.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @PreAuthorize("hasAnyRole('ADMIN', 'KOORDINATOR_LOMBA', 'KOORDINATOR_PENDAFTARAN')")
    @GetMapping("/analytics")
    public ResponseEntity<BaseResponse<AnalyticsResponse>> getAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start_date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end_date,
            @RequestParam(required = false) List<String> jenis_burung,
            @RequestParam(required = false) List<String> kelas) {

        AnalyticsResponse data = dashboardService.getAnalytics(start_date, end_date, jenis_burung, kelas);
        return ResponseEntity.ok(BaseResponse.success(data, "Analytics berhasil diambil"));
    }
}
