package com.propinsi.backend.dashboard.service;

import com.propinsi.backend.dashboard.restdto.response.AnalyticsResponse;
import com.propinsi.backend.dashboard.restdto.response.BirdTypeSalesResponse;
import com.propinsi.backend.dashboard.restdto.response.ClassSalesResponse;
import com.propinsi.backend.dashboard.restdto.response.TrendDataResponse;
import com.propinsi.backend.mengelola_lomba.model.JenisBurung;
import com.propinsi.backend.pendaftaran_lomba.model.Reservasi;
import com.propinsi.backend.pendaftaran_lomba.model.StatusReservasi;
import com.propinsi.backend.pendaftaran_lomba.repository.ReservasiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ReservasiRepository reservasiRepository;

    @Override
    public AnalyticsResponse getAnalytics(LocalDate startDate, LocalDate endDate, String jenisBurung, String kelas) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Reservasi> paid = reservasiRepository
                .findByStatusAndWaktuReservasiBetween(StatusReservasi.PAID, start, end)
                .stream()
                .filter(r -> jenisBurung == null || r.getLomba().getJenisBurung().name().equalsIgnoreCase(jenisBurung))
                .filter(r -> kelas == null || r.getLomba().getKelas().equalsIgnoreCase(kelas))
                .collect(Collectors.toList());

        long total = paid.size();

        Map<String, List<Reservasi>> byKelas = paid.stream()
                .collect(Collectors.groupingBy(r -> r.getLomba().getKelas()));

        List<ClassSalesResponse> allClasses = byKelas.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().size(), a.getValue().size()))
                .map(e -> {
                    long count = e.getValue().size();
                    LocalDate min = e.getValue().stream()
                            .map(r -> r.getWaktuReservasi().toLocalDate())
                            .min(LocalDate::compareTo).orElse(null);
                    LocalDate max = e.getValue().stream()
                            .map(r -> r.getWaktuReservasi().toLocalDate())
                            .max(LocalDate::compareTo).orElse(null);
                    List<TrendDataResponse> daily = toDailyBreakdown(e.getValue());
                    return new ClassSalesResponse(e.getKey(), toPercent(count, total), count,
                            min != null ? min.toString() : null,
                            max != null ? max.toString() : null,
                            daily);
                })
                .collect(Collectors.toList());

        List<ClassSalesResponse> top5Classes = allClasses.stream().limit(5).collect(Collectors.toList());

        Map<JenisBurung, List<Reservasi>> byBird = paid.stream()
                .collect(Collectors.groupingBy(r -> r.getLomba().getJenisBurung()));

        List<BirdTypeSalesResponse> allBirdTypes = byBird.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().size(), a.getValue().size()))
                .map(e -> {
                    long count = e.getValue().size();
                    LocalDate min = e.getValue().stream()
                            .map(r -> r.getWaktuReservasi().toLocalDate())
                            .min(LocalDate::compareTo).orElse(null);
                    LocalDate max = e.getValue().stream()
                            .map(r -> r.getWaktuReservasi().toLocalDate())
                            .max(LocalDate::compareTo).orElse(null);
                    List<TrendDataResponse> daily = toDailyBreakdown(e.getValue());
                    return new BirdTypeSalesResponse(formatJenisBurung(e.getKey().name()), toPercent(count, total), count,
                            min != null ? min.toString() : null,
                            max != null ? max.toString() : null,
                            daily);
                })
                .collect(Collectors.toList());

        List<BirdTypeSalesResponse> top5BirdTypes = allBirdTypes.stream().limit(5).collect(Collectors.toList());

        List<TrendDataResponse> trendData = paid.stream()
                .collect(Collectors.groupingBy(r -> r.getWaktuReservasi().toLocalDate(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new TrendDataResponse(e.getKey().toString(), e.getValue()))
                .collect(Collectors.toList());

        return AnalyticsResponse.builder()
                .top5Classes(top5Classes)
                .top5BirdTypes(top5BirdTypes)
                .trendData(trendData)
                .allClasses(allClasses)
                .allBirdTypes(allBirdTypes)
                .build();
    }

    private List<TrendDataResponse> toDailyBreakdown(List<Reservasi> reservasiList) {
        return reservasiList.stream()
                .collect(Collectors.groupingBy(r -> r.getWaktuReservasi().toLocalDate(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new TrendDataResponse(e.getKey().toString(), e.getValue()))
                .collect(Collectors.toList());
    }

    private double toPercent(long count, long total) {
        return total == 0 ? 0.0 : Math.round((double) count / total * 1000.0) / 10.0;
    }

    private String formatJenisBurung(String enumName) {
        String[] words = enumName.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(" ");
            result.append(word.charAt(0)).append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }
}
