package com.propinsi.backend.dashboard.service;

import com.propinsi.backend.dashboard.restdto.response.AnalyticsResponse;

import java.time.LocalDate;

public interface DashboardService {
    AnalyticsResponse getAnalytics(LocalDate startDate, LocalDate endDate, String jenisBurung, String kelas);
}
