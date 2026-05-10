package com.propinsi.backend.dashboard.service;

import com.propinsi.backend.dashboard.restdto.response.AnalyticsResponse;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    AnalyticsResponse getAnalytics(LocalDate startDate, LocalDate endDate, List<String> jenisBurung, List<String> kelas);
}
