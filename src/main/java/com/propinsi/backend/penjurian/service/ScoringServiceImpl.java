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
import com.propinsi.backend.penjurian.restdto.request.ScoringVoteRequest;
import com.propinsi.backend.penjurian.restdto.response.ScoringBlokDetailResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringBlokSummaryResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringGantanganResponse;
import com.propinsi.backend.penjurian.restdto.response.ScoringVoteResponse;
import com.propinsi.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

        Lomba lomba = getAssignedLomba(lombaId, currentJuri.getId());

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
        int updatedWarning = Math.min(3, currentWarning + 1);
        gantangan.setWarningCount(updatedWarning);

        if (updatedWarning >= 3) {
            gantangan.setStatus(GantanganStatus.DISQUALIFIED);
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

        if (!isBooked(gantangan)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Gantangan belum dibooking peserta");
        }

        gantangan.setStatus(GantanganStatus.DISQUALIFIED);
        int currentWarning = gantangan.getWarningCount() == null ? 0 : gantangan.getWarningCount();
        gantangan.setWarningCount(Math.max(currentWarning, 1));

        return toGantanganResponse(gantanganRepository.save(gantangan));
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
        return !Boolean.TRUE.equals(g.getIsAvailable());
    }
}
