package com.propinsi.backend.dashboard.service;

import com.propinsi.backend.dashboard.restdto.response.AnalyticsResponse;
import com.propinsi.backend.dashboard.restdto.response.BirdTypeSalesResponse;
import com.propinsi.backend.dashboard.restdto.response.ClassSalesResponse;
import com.propinsi.backend.dashboard.restdto.response.TrendDataResponse;
import com.propinsi.backend.mengelola_lomba.model.JenisBurung;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.pendaftaran_lomba.model.Reservasi;
import com.propinsi.backend.pendaftaran_lomba.model.StatusReservasi;
import com.propinsi.backend.pendaftaran_lomba.repository.ReservasiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ReservasiRepository reservasiRepository;

    @Override
    public AnalyticsResponse getAnalytics(LocalDate startDate, LocalDate endDate, List<String> jenisBurung, List<String> kelas) {
        validateDateRange(startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        List<JenisBurung> selectedJenisBurung = parseJenisBurungFilter(jenisBurung);
        List<String> selectedKelas = normalizeTextFilter(kelas);

        List<Reservasi> paid = reservasiRepository
                .findByStatusAndWaktuReservasiGreaterThanEqualAndWaktuReservasiLessThan(StatusReservasi.PAID, start, end)
                .stream()
                .filter(r -> r.getLomba() != null)
                .filter(r -> matchesJenisBurung(r, selectedJenisBurung))
                .filter(r -> matchesKelas(r, selectedKelas))
                .collect(Collectors.toList());

        long total = paid.size();

        long totalRevenue = paid.stream()
                .mapToLong(r -> r.getNominal() != null ? r.getNominal().longValue() : 0L)
                .sum();

        List<Reservasi> allReservasi = reservasiRepository
                .findByWaktuReservasiGreaterThanEqualAndWaktuReservasiLessThan(start, end)
                .stream()
                .filter(r -> r.getLomba() != null)
                .filter(r -> matchesJenisBurung(r, selectedJenisBurung))
                .filter(r -> matchesKelas(r, selectedKelas))
                .collect(Collectors.toList());

        long allCount = allReservasi.size();
        double bookingSuccessRate = allCount == 0 ? 0.0 : Math.round((double) total / allCount * 1000.0) / 10.0;

        List<Lomba> finishedLombas = paid.stream()
                .map(Reservasi::getLomba)
                .distinct()
                .filter(l -> StatusLomba.SELESAI.equals(l.getStatus()))
                .collect(Collectors.toList());

        long totalFinishedCapacity = finishedLombas.stream()
                .mapToInt(l -> l.getListGantangan() != null ? l.getListGantangan().size() : 0)
                .sum();

        long totalPresent = finishedLombas.stream()
                .flatMap(l -> l.getListGantangan() != null ? l.getListGantangan().stream() : java.util.stream.Stream.empty())
                .filter(g -> Boolean.TRUE.equals(g.getIsPresent()))
                .count();

        double occupancyRate = totalFinishedCapacity == 0 ? 0.0 : Math.round((double) totalPresent / totalFinishedCapacity * 1000.0) / 10.0;

        Map<String, List<Reservasi>> byKelas = paid.stream()
                .filter(r -> r.getLomba() != null && r.getLomba().getKelas() != null)
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
                .filter(r -> r.getLomba() != null && r.getLomba().getJenisBurung() != null)
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
                .totalTiketTerjual(total)
                .totalRevenue(totalRevenue)
                .bookingSuccessRate(bookingSuccessRate)
                .occupancyRate(occupancyRate)
                .top5Classes(top5Classes)
                .top5BirdTypes(top5BirdTypes)
                .trendData(trendData)
                .allClasses(allClasses)
                .allBirdTypes(allBirdTypes)
                .build();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start_date dan end_date wajib diisi");
        }

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start_date tidak boleh lebih dari end_date");
        }
    }

    private List<JenisBurung> parseJenisBurungFilter(List<String> jenisBurung) {
        List<String> normalized = normalizeTextFilter(jenisBurung);
        if (normalized.isEmpty()) {
            return List.of();
        }

        Set<String> validValues = Arrays.stream(JenisBurung.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        return normalized.stream()
                .map(value -> value.toUpperCase().replace(" ", "_"))
                .map(value -> {
                    if (!validValues.contains(value)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jenis_burung tidak valid: " + value);
                    }
                    return JenisBurung.valueOf(value);
                })
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> normalizeTextFilter(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean matchesJenisBurung(Reservasi reservasi, List<JenisBurung> selectedJenisBurung) {
        return selectedJenisBurung.isEmpty()
                || (reservasi.getLomba().getJenisBurung() != null
                && selectedJenisBurung.contains(reservasi.getLomba().getJenisBurung()));
    }

    private boolean matchesKelas(Reservasi reservasi, List<String> selectedKelas) {
        return selectedKelas.isEmpty()
                || (reservasi.getLomba().getKelas() != null
                && selectedKelas.stream().anyMatch(k -> reservasi.getLomba().getKelas().equalsIgnoreCase(k)));
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
