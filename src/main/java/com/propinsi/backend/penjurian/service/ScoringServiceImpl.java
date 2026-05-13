package com.propinsi.backend.penjurian.service;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.GantanganStatus;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.repository.GantanganRepository;
import com.propinsi.backend.mengelola_lomba.repository.LombaRepository;
import com.propinsi.backend.model.Role;
import com.propinsi.backend.model.User;
import com.propinsi.backend.penjurian.model.ScoringVote;
import com.propinsi.backend.penjurian.repository.ScoringVoteRepository;
import com.propinsi.backend.penjurian.repository.KoncerVoteRepository;
import com.propinsi.backend.penjurian.restdto.request.KoncerVoteSubmitRequest;
import com.propinsi.backend.penjurian.restdto.response.KoncerStatusResponse;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import com.propinsi.backend.penjurian.model.KoncerVote;
import com.propinsi.backend.penjurian.restdto.request.KoncerVoteItemRequest;
import com.propinsi.backend.penjurian.restdto.request.ScoringVoteRequest;
import com.propinsi.backend.penjurian.restdto.response.GantanganRankingResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringBlokDetailResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringBlokSummaryResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringGantanganResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringVoteResponse;
import com.propinsi.backend.penjurian.restdto.response.SemiFinalStandingsResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.FinalResultResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.FinalResultGantanganResponse;
import com.propinsi.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScoringServiceImpl implements ScoringService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LombaRepository lombaRepository;

    @Autowired
    private GantanganRepository gantanganRepository;

    @Autowired
    private ScoringVoteRepository scoringVoteRepository;
    
    @Autowired
    private KoncerVoteRepository koncerVoteRepository;

    @Override
    public List<ScoringBlokSummaryResponse> getBlokSummary(UUID lombaId, Long juriId) {
        User juri = getJuriById(juriId);
        Lomba lomba = getAssignedLomba(lombaId, juri.getId());

        List<ScoringBlokSummaryResponse> result = new ArrayList<>();
        for (int blokId = 1; blokId <= 4; blokId++) {
            boolean locked = scoringVoteRepository.existsByJuriIdAndLombaIdAndBlokId(juri.getId(), lomba.getId(), blokId);
            result.add(
                ScoringBlokSummaryResponse.builder()
                    .blokId(blokId)
                    .blokLabel(toRomanLabel(blokId))
                    .locked(locked)
                    .build()
            );
        }
        return result;
    }

    @Override
    public ScoringBlokDetailResponse getBlokDetail(UUID lombaId, Long juriId, Integer blokId) {
        validateBlokId(blokId);

        User juri = getJuriById(juriId);
        Lomba lomba = getAssignedLomba(lombaId, juri.getId());
        List<Gantangan> allGantangan = gantanganRepository.findByLombaIdOrderByNomorGantanganAsc(lomba.getId());
        List<Gantangan> blockGantangan = getGantanganByBlok(allGantangan, blokId);

        boolean locked = scoringVoteRepository.existsByJuriIdAndLombaIdAndBlokId(juri.getId(), lomba.getId(), blokId);

        return ScoringBlokDetailResponse.builder()
            .blokId(blokId)
            .blokLabel(toRomanLabel(blokId))
            .lombaId(lomba.getId())
            .namaLomba(lomba.getNamaLomba())
            .locked(locked)
            .gantangan(blockGantangan.stream().map(this::toGantanganResponse).toList())
            .build();
    }

    @Override
    @Transactional
    public ScoringVoteResponse submitVote(UUID lombaId, Long juriId, ScoringVoteRequest request) {
        validateBlokId(request.getBlokId());

        User currentJuri = getJuriById(juriId);

        Lomba lombaInfo = getAssignedLomba(lombaId, currentJuri.getId());
        Lomba lomba = lombaRepository.findByIdForUpdate(lombaInfo.getId()).orElse(lombaInfo);

        List<Gantangan> allGantangan = gantanganRepository.findByLombaIdOrderByNomorGantanganAsc(lomba.getId());
        List<Gantangan> blockGantangan = getGantanganByBlok(allGantangan, request.getBlokId());
        Set<UUID> blockIds = blockGantangan.stream().map(Gantangan::getId).collect(Collectors.toSet());

        List<UUID> requestedIds = request.getGantanganIds() == null ? Collections.emptyList() : request.getGantanganIds();

        for (UUID gantanganId : requestedIds) {
            if (!blockIds.contains(gantanganId)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "gantangan_id tidak termasuk blok yang dipilih");
            }
        }

        List<Gantangan> selectedGantangan = requestedIds.isEmpty()
            ? Collections.emptyList()
            : gantanganRepository.findByIdIn(requestedIds);

        if (selectedGantangan.size() != requestedIds.size()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Ada gantangan_id yang tidak ditemukan");
        }

        boolean hasInvalidSelection = selectedGantangan.stream()
            .anyMatch(g -> g.getStatus() == GantanganStatus.DISQUALIFIED || !isBooked(g));

        if (hasInvalidSelection) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Ajuan ditolak karena ada gantangan DISQUALIFIED atau is_booked=false");
        }

        ScoringVote vote = scoringVoteRepository
            .findByJuriIdAndLombaIdAndBlokId(currentJuri.getId(), lomba.getId(), request.getBlokId())
            .orElseGet(() -> ScoringVote.builder()
                .juri(currentJuri)
                .lomba(lomba)
                .blokId(request.getBlokId())
                .build());

        vote.setSelectedGantangans(new HashSet<>(selectedGantangan));
        scoringVoteRepository.save(vote);
        scoringVoteRepository.flush(); // Ensure vote is flushed so getSemiFinalStandings sees it

        SemiFinalStandingsResponse semiFinal = getSemiFinalStandings(lombaId);
        if ("FINISH".equals(semiFinal.getNextStep()) && lomba.getStatus() != StatusLomba.SELESAI) {
            lomba.setStatus(StatusLomba.SELESAI);
            lombaRepository.save(lomba);
        }

        return ScoringVoteResponse.builder()
            .message("Ajuan gantangan berhasil disimpan")
            .blokId(request.getBlokId())
            .locked(true)
            .build();
    }

    @Override
    @Transactional
    public ScoringGantanganResponse addWarning(UUID lombaId, Long juriId, UUID gantanganId) {
        getJuriById(juriId);
        Lomba lomba = getAssignedLomba(lombaId, juriId);
        Gantangan gantangan = findGantangan(gantanganId);

        if (!gantangan.getLomba().getId().equals(lomba.getId())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Gantangan tidak berada pada lomba yang dipilih");
        }

        if (!isBooked(gantangan)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Gantangan belum dibooking peserta");
        }

        if (gantangan.getStatus() == GantanganStatus.DISQUALIFIED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Gantangan sudah didiskualifikasi");
        }

        int currentWarning = gantangan.getWarningCount() == null ? 0 : gantangan.getWarningCount();
        int updatedWarning = currentWarning + 1;
        gantangan.setWarningCount(updatedWarning);

        if (updatedWarning >= 3) {
            gantangan.setStatus(GantanganStatus.DISQUALIFIED);
            removeVotesForGantangan(gantangan);
        }

        return toGantanganResponse(gantanganRepository.save(gantangan));
    }

    @Override
    @Transactional
    public ScoringGantanganResponse disqualify(UUID lombaId, Long juriId, UUID gantanganId) {
        getJuriById(juriId);
        Lomba lomba = getAssignedLomba(lombaId, juriId);
        Gantangan gantangan = findGantangan(gantanganId);

        if (!gantangan.getLomba().getId().equals(lomba.getId())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Gantangan tidak berada pada lomba yang dipilih");
        }

        if (gantangan.getStatus() == GantanganStatus.DISQUALIFIED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Gantangan sudah didiskualifikasi");
        }

        if (!isBooked(gantangan)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Gantangan belum dibooking peserta");
        }

        gantangan.setStatus(GantanganStatus.DISQUALIFIED);
        
        removeVotesForGantangan(gantangan);

        return toGantanganResponse(gantanganRepository.save(gantangan));
    }

    private void removeVotesForGantangan(Gantangan gantangan) {
        List<ScoringVote> votes = scoringVoteRepository.findBySelectedGantangansContains(gantangan);
        for (ScoringVote vote : votes) {
            vote.getSelectedGantangans().remove(gantangan);
            scoringVoteRepository.save(vote);
        }
    }

    private User getJuriById(Long juriId) {
        User user = userRepository.findById(juriId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User tidak ditemukan"));

        if (user.getRole() != Role.JURI) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Fitur ini hanya untuk role JURI");
        }

        return user;
    }

    private Lomba getAssignedLomba(UUID lombaId, Long juriId) {
        Lomba lomba = lombaRepository.findById(lombaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lomba tidak ditemukan"));

        boolean assigned = lomba.getListJuri() != null && lomba.getListJuri().stream()
            .anyMatch(juri -> juri.getId().equals(juriId));

        if (!assigned) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Juri tidak di-assign ke lomba ini");
        }

        return lomba;
    }

    private Gantangan findGantangan(UUID gantanganId) {
        return gantanganRepository.findById(gantanganId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gantangan tidak ditemukan"));
    }

    private List<Gantangan> getGantanganByBlok(List<Gantangan> gantangan, Integer blokId) {
        List<Integer> targetNomor = new ArrayList<>();
        if (blokId == 1) {
            targetNomor = java.util.Arrays.asList(9, 10, 8, 7, 1, 2);
        } else if (blokId == 2) {
            targetNomor = java.util.Arrays.asList(24, 23, 17, 18, 16, 15);
        } else if (blokId == 3) {
            targetNomor = java.util.Arrays.asList(22, 21, 19, 20, 14, 13);
        } else if (blokId == 4) {
            targetNomor = java.util.Arrays.asList(11, 12, 6, 5, 3, 4);
        }

        List<Gantangan> result = new ArrayList<>();
        for (Integer num : targetNomor) {
            gantangan.stream()
                .filter(g -> g.getNomorGantangan().equals(num))
                .findFirst()
                .ifPresent(result::add);
        }
        return result;
    }

    private void validateBlokId(Integer blokId) {
        if (blokId == null || blokId < 1 || blokId > 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "blok_id harus di antara 1 sampai 4");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SemiFinalStandingsResponse getSemiFinalStandings(UUID lombaId) {
        Lomba lomba = lombaRepository.findById(lombaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lomba tidak ditemukan"));

        List<ScoringVote> allVotes = scoringVoteRepository.findByLombaId(lombaId);

        // 1. Hitung Progress Juri
        Map<Long, Long> judgeProgress = allVotes.stream()
                .collect(Collectors.groupingBy(v -> v.getJuri().getId(), Collectors.counting()));
        long finishedJudges = judgeProgress.values().stream().filter(count -> count == 4).count();

        // 2. Olah Klasemen
        List<Gantangan> allGantangans = gantanganRepository.findByLombaIdOrderByNomorGantanganAsc(lombaId)
                .stream().distinct().collect(Collectors.toList());

        List<GantanganRankingResponse> rankings = new ArrayList<>();
        Set<Integer> uniqueCheck = new HashSet<>();

        for (Gantangan g : allGantangans) {
            // Hanya proses jika ACTIVE dan nomor belum pernah diproses
            if (g.getStatus() == GantanganStatus.ACTIVE && !uniqueCheck.contains(g.getNomorGantangan())) {
                long voteCount = allVotes.stream()
                        .filter(v -> v.getSelectedGantangans().stream().anyMatch(sg -> sg.getId().equals(g.getId())))
                        .count();

                if (voteCount > 0) {
                    rankings.add(GantanganRankingResponse.builder()
                            .nomorGantangan(g.getNomorGantangan())
                            .blokId(getBlokIdByNomor(g.getNomorGantangan()))
                            .jumlahAjuan(voteCount).build());
                    uniqueCheck.add(g.getNomorGantangan());
                }
            }
        }

        // Urutkan (Tertinggi di atas)
        rankings.sort(Comparator.comparing(GantanganRankingResponse::getJumlahAjuan).reversed());

        String nextStep = "WAITING";
        List<GantanganRankingResponse> koncerQualifiers = new ArrayList<>();
        int targetJuri = 4;

        if (finishedJudges >= targetJuri) {
            if (!rankings.isEmpty()) {
                // Ambil nilai ajuan paling tinggi yang ada di tabel
                long highestVote = rankings.get(0).getJumlahAjuan();
                
                // Cari ada berapa burung yang punya nilai SAMA dengan highestVote
                koncerQualifiers = rankings.stream()
                        .filter(r -> r.getJumlahAjuan() == highestVote)
                        .collect(Collectors.toList());

                // Jika cuma ada SATU burung di list koncerQualifiers -> FINISH (Pemenang Mutlak)
                if (koncerQualifiers.size() == 1) {
                    nextStep = "FINISH";
                    // Bersihkan list koncer karena juri gak perlu adu koncer lagi
                    koncerQualifiers = new ArrayList<>(); 
                } else {
                    // Jika ada 2 atau lebih burung dengan nilai tertinggi yang sama -> KONCER (Seri)
                    nextStep = "KONCER";
                }
            } else {
                // Case kalau semua burung DQ atau gak ada yang vote
                nextStep = "FINISH"; 
            }
        }

        return SemiFinalStandingsResponse.builder()
                .lombaId(lombaId).namaLomba(lomba.getNamaLomba())
                .juriSubmitted((int) finishedJudges).totalJuri(targetJuri)
                .nextStep(nextStep).rankings(rankings)
                .koncerQualifiers(koncerQualifiers).build();
    }

    // Helper untuk menentukan Blok ID berdasarkan Nomor Gantangan (sesuaikan dengan logic Nia)
    private Integer getBlokIdByNomor(Integer nomor) {
        if (java.util.Arrays.asList(9, 10, 8, 7, 1, 2).contains(nomor)) return 1;
        if (java.util.Arrays.asList(24, 23, 17, 18, 16, 15).contains(nomor)) return 2;
        if (java.util.Arrays.asList(22, 21, 19, 20, 14, 13).contains(nomor)) return 3;
        return 4;
    }

    private String toRomanLabel(Integer blokId) {
        return switch (blokId) {
            case 1 -> "BLOK I";
            case 2 -> "BLOK II";
            case 3 -> "BLOK III";
            case 4 -> "BLOK IV";
            default -> "BLOK";
        };
    }

    private ScoringGantanganResponse toGantanganResponse(Gantangan g) {
        return ScoringGantanganResponse.builder()
            .id(g.getId())
            .nomorGantangan(g.getNomorGantangan())
            .status(g.getStatus() == null ? GantanganStatus.ACTIVE : g.getStatus().normalized())
            .warningCount(g.getWarningCount() == null ? 0 : g.getWarningCount())
            .isBooked(isBooked(g))
            .build();
    }

    private boolean isBooked(Gantangan g) {
        return g.getStatus() == GantanganStatus.ACTIVE || g.getStatus() == GantanganStatus.DISQUALIFIED;
    }

    @Override
    public KoncerStatusResponse getKoncerStatus(UUID lombaId, Long juriId) {
        getJuriById(juriId);
        Lomba lomba = getAssignedLomba(lombaId, juriId);
        
        boolean hasSubmitted = koncerVoteRepository.existsByLombaIdAndJuriId(lomba.getId(), juriId);
        long totalSubmitted = koncerVoteRepository.countDistinctJuriByLombaId(lomba.getId());
        int jumlahJuri = lomba.getListJuri() != null ? lomba.getListJuri().size() : lomba.getJumlahJuri();
        if (jumlahJuri == 0) jumlahJuri = lomba.getJumlahJuri();
        boolean isFinished = (totalSubmitted >= jumlahJuri);

        java.util.Map<String, String> userVotes = null;
        if (hasSubmitted) {
            java.util.List<com.propinsi.backend.penjurian.model.KoncerVote> votes = koncerVoteRepository.findByLombaIdAndJuriId(lomba.getId(), juriId);
            userVotes = votes.stream()
                .collect(java.util.stream.Collectors.toMap(
                    v -> v.getGantangan().getId().toString(),
                    v -> v.getPoin().toString()
                ));
        }

        return KoncerStatusResponse.builder()
            .hasSubmitted(hasSubmitted)
            .totalJuriSubmitted(totalSubmitted)
            .isKoncerFinished(isFinished)
            .userVotes(userVotes)
            .build();
    }

    @Override
    @Transactional
    public void submitKoncer(UUID lombaId, Long juriId, KoncerVoteSubmitRequest request) {
        User juri = getJuriById(juriId);
        Lomba lombaInfo = getAssignedLomba(lombaId, juriId);
        
        // Lock the lomba to prevent race conditions during submission and status updates
        Lomba lomba = lombaRepository.findByIdForUpdate(lombaInfo.getId()).orElse(lombaInfo);

        if (lomba.getStatus() == StatusLomba.SELESAI) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lomba sudah selesai");
        }

        if (koncerVoteRepository.existsByLombaIdAndJuriId(lomba.getId(), juriId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anda sudah melakukan submit babak koncer");
        }

        if (request.getVotes() == null || request.getVotes().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vote tidak boleh kosong");
        }

        // Validate multiple vote same gantangan from same juri
        Set<UUID> seenGantangan = new HashSet<>();
        for (KoncerVoteItemRequest voteItem : request.getVotes()) {
            if (!seenGantangan.add(voteItem.getGantanganId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tidak boleh memberikan vote lebih dari 1 kali untuk gantangan yang sama");
            }
        }

        List<KoncerVote> votesToSave = new ArrayList<>();
        for (KoncerVoteItemRequest item : request.getVotes()) {
            Gantangan gantangan = gantanganRepository.findById(item.getGantanganId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gantangan tidak ditemukan: " + item.getGantanganId()));
            
            if (!gantangan.getLomba().getId().equals(lomba.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gantangan tidak ada dalam lomba ini");
            }

            KoncerVote koncerVote = KoncerVote.builder()
                .lomba(lomba)
                .juri(juri)
                .gantangan(gantangan)
                .poin(item.getPoin())
                .build();
            votesToSave.add(koncerVote);
        }

        koncerVoteRepository.saveAll(votesToSave);

        // Check format juri after insert
        long totalSubmitted = koncerVoteRepository.countDistinctJuriByLombaId(lomba.getId());
        int jumlahJuri = lomba.getListJuri() != null ? lomba.getListJuri().size() : lomba.getJumlahJuri();
        if (jumlahJuri == 0) jumlahJuri = lomba.getJumlahJuri();
        
        if (totalSubmitted >= jumlahJuri && lomba.getStatus() != StatusLomba.SELESAI) {
            lomba.setStatus(StatusLomba.SELESAI);
            lombaRepository.save(lomba);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FinalResultResponse getFinalResult(UUID lombaId) {
        Lomba lomba = lombaRepository.findById(lombaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lomba tidak ditemukan"));

        SemiFinalStandingsResponse semiFinal = getSemiFinalStandings(lombaId);
        List<GantanganRankingResponse> rankings = semiFinal.getRankings();
        String nextStep = semiFinal.getNextStep();
        
        List<Gantangan> allGantangan = gantanganRepository.findByLombaIdOrderByNomorGantanganAsc(lombaId);
        
        Map<Integer, Long> ajuanMap = rankings.stream()
            .collect(Collectors.toMap(GantanganRankingResponse::getNomorGantangan, GantanganRankingResponse::getJumlahAjuan));
            
        List<FinalResultGantanganResponse> results = new ArrayList<>();
        
        if ("FINISH".equals(nextStep)) {
            List<Long> distinctAjuans = rankings.stream().map(GantanganRankingResponse::getJumlahAjuan).distinct().sorted(Comparator.reverseOrder()).toList();
            Long rank1Ajuan = distinctAjuans.size() > 0 ? distinctAjuans.get(0) : null;
            Long rank2Ajuan = distinctAjuans.size() > 1 ? distinctAjuans.get(1) : null;
            
            for (Gantangan g : allGantangan) {
                Long ajuan = ajuanMap.getOrDefault(g.getNomorGantangan(), 0L);
                String hasilKoncer = null;
                Integer totalPoin = null;
                
                if (ajuan > 0) {
                    if (rank1Ajuan != null && ajuan.equals(rank1Ajuan)) {
                        hasilKoncer = "A";
                        totalPoin = 100;
                    } else if (rank2Ajuan != null && ajuan.equals(rank2Ajuan)) {
                        hasilKoncer = "B";
                        totalPoin = 40;
                    }
                }
                
                results.add(FinalResultGantanganResponse.builder()
                    .nomorGantangan(g.getNomorGantangan())
                    .totalAjuan(ajuan)
                    .hasilKoncer(hasilKoncer)
                    .totalPoin(totalPoin)
                    .build());
            }
        } else {
            List<KoncerVote> koncerVotes = koncerVoteRepository.findByLombaId(lombaId);
            Map<Integer, List<KoncerVote>> votesByGantangan = koncerVotes.stream()
                .collect(Collectors.groupingBy(v -> v.getGantangan().getNomorGantangan()));
            
            Set<Integer> qualifiedNomors = (semiFinal.getKoncerQualifiers() != null) ? 
                semiFinal.getKoncerQualifiers().stream().map(GantanganRankingResponse::getNomorGantangan).collect(Collectors.toSet()) 
                : new HashSet<>();
                
            for (Gantangan g : allGantangan) {
                Long ajuan = ajuanMap.getOrDefault(g.getNomorGantangan(), 0L);
                
                String hasilKoncer = null;
                Integer totalPoin = null;
                
                boolean isQualified = qualifiedNomors.contains(g.getNomorGantangan());
                
                if (isQualified || votesByGantangan.containsKey(g.getNomorGantangan())) {
                    List<KoncerVote> gVotes = votesByGantangan.getOrDefault(g.getNomorGantangan(), Collections.emptyList());
                    int aCount = 0;
                    int bCount = 0;
                    StringBuilder sb = new StringBuilder();
                    for (KoncerVote v : gVotes) {
                        if (v.getPoin() != null) {
                            sb.append(v.getPoin().name());
                            if (v.getPoin().name().equals("A")) aCount++;
                            else if (v.getPoin().name().equals("B")) bCount++;
                        }
                    }
                    
                    char[] chars = sb.toString().toCharArray();
                    java.util.Arrays.sort(chars);
                    hasilKoncer = new String(chars);
                    totalPoin = (aCount * 100) + (bCount * 40);
                }
                
                results.add(FinalResultGantanganResponse.builder()
                    .nomorGantangan(g.getNomorGantangan())
                    .totalAjuan(ajuan)
                    .hasilKoncer(hasilKoncer)
                    .totalPoin(totalPoin)
                    .build());
            }
        }
        
        results.sort((r1, r2) -> {
            Integer p1 = r1.getTotalPoin() == null ? -1 : r1.getTotalPoin();
            Integer p2 = r2.getTotalPoin() == null ? -1 : r2.getTotalPoin();
            int pCompare = p2.compareTo(p1);
            if (pCompare != 0) return pCompare;
            
            return r1.getNomorGantangan().compareTo(r2.getNomorGantangan());
        });
        
        return new FinalResultResponse(results);
    }
}
