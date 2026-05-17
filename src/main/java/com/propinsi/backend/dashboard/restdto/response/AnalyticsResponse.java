package com.propinsi.backend.dashboard.restdto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalyticsResponse {
    private long totalTiketTerjual;
    private long totalRevenue;
    private double occupancyRate;
    private double attendanceRate;
    
    private List<ClassSalesResponse> top5Classes;
    private List<BirdTypeSalesResponse> top5BirdTypes;
    private List<TrendDataResponse> trendData;
    private List<ClassSalesResponse> allClasses;
    private List<BirdTypeSalesResponse> allBirdTypes;
}
