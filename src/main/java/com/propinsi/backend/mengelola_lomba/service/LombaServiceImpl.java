package com.propinsi.backend.mengelola_lomba.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.GantanganStatus;
import com.propinsi.backend.mengelola_lomba.model.JenisBurung;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import com.propinsi.backend.mengelola_lomba.repository.GantanganRepository;
import com.propinsi.backend.mengelola_lomba.repository.LombaRepository;
import com.propinsi.backend.mengelola_lomba.repository.LombaSpecification;
import com.propinsi.backend.mengelola_lomba.restdto.request.AssignJuriRequest;
import com.propinsi.backend.mengelola_lomba.restdto.request.LombaRequest;
import com.propinsi.backend.mengelola_lomba.restdto.response.GantanganResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.LombaDetailResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.LombaResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.UserSummaryResponse;
import com.propinsi.backend.model.Role;
import com.propinsi.backend.model.User;
import com.propinsi.backend.repository.UserRepository;

@Service
@Transactional
public class LombaServiceImpl implements LombaService {

    @Autowired
    private LombaRepository lombaRepository;

    @Autowired
    private GantanganRepository gantanganRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public LombaResponse createLomba(LombaRequest request) {
        Lomba lomba = new Lomba();
        lomba.setNamaLomba(request.getNamaLomba());
        lomba.setLokasi(request.getLokasi());
        lomba.setWaktuTanggal(request.getWaktuTanggal());
        lomba.setJenisBurung(request.getJenisBurung());
        lomba.setKelas(request.getKelas());
        lomba.setHargaTiket(request.getHargaTiket());
        lomba.setHadiah(request.getHadiah());
        lomba.setJumlahJuara(request.getHadiah() != null ? request.getHadiah().size() : 0);
        lomba.setJumlahJuri(request.getJumlahJuri());
        lomba.setContactPerson(request.getContactPerson());
        lomba.setStatus(StatusLomba.BELUM_DIMULAI);
        lomba.setDeskripsi(request.getDeskripsi());

        Lomba savedLomba = lombaRepository.save(lomba);

        List<Gantangan> listGantangan = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            Gantangan gantangan = new Gantangan();
            gantangan.setBlok(1);
            gantangan.setNomorGantangan(i);
            gantangan.setStatus(GantanganStatus.AVAILABLE); 
            gantangan.setLomba(savedLomba);
            listGantangan.add(gantangan);
        }
        gantanganRepository.saveAll(listGantangan);
        savedLomba.setListGantangan(listGantangan);

        return mapToLombaResponse(savedLomba);
    }

    @Override
    public LombaResponse getLombaById(UUID id) {
        Lomba lomba = lombaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lomba tidak ditemukan"));
        return mapToLombaResponse(lomba);
    }

    @Override
    public List<LombaResponse> getAllLomba() {
        return getAllLomba(null, null, null, null, false);
    }

    @Override
    public List<LombaResponse> getAllLomba(String jenisBurung, String status, String sortBy, String sortDir, boolean includeAll) {
        JenisBurung jenisBurungEnum = null;
        if (jenisBurung != null && !jenisBurung.isEmpty()) {
            try { jenisBurungEnum = JenisBurung.valueOf(jenisBurung); } catch (IllegalArgumentException ignored) {}
        }

        StatusLomba statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try { statusEnum = StatusLomba.valueOf(status); } catch (IllegalArgumentException ignored) {}
        }

        final JenisBurung finalJenis = jenisBurungEnum;
        final StatusLomba finalStatus = statusEnum;

        List<Lomba> lombaList;

        if (includeAll) {
            // Admin & Koordinator: ambil semua termasuk yang status=DIBATALKAN
            lombaList = lombaRepository.findAllIncludingDeleted();
            // Filter manual di Java
            lombaList = lombaList.stream()
                    .filter(l -> finalJenis == null || l.getJenisBurung() == finalJenis)
                    .filter(l -> finalStatus == null || l.getStatus() == finalStatus)
                    .collect(Collectors.toList());
        } else {
            // Peserta & lainnya: status DIBATALKAN otomatis dikecualikan via @Where
            Sort sort = Sort.unsorted();
            if (sortBy != null && !sortBy.isEmpty()) {
                Sort.Direction dir = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
                sort = Sort.by(dir, sortBy);
            }
            lombaList = lombaRepository.findAll(LombaSpecification.filter(finalJenis, finalStatus, true), sort);
        }

        // Sorting untuk includeAll path
        if (includeAll && sortBy != null && !sortBy.isEmpty()) {
            final boolean desc = "desc".equalsIgnoreCase(sortDir);
            lombaList = lombaList.stream().sorted((a, b) -> {
                try {
                    java.lang.reflect.Method getter = Lomba.class.getMethod(
                            "get" + sortBy.substring(0, 1).toUpperCase() + sortBy.substring(1));
                    Comparable valA = (Comparable) getter.invoke(a);
                    Comparable valB = (Comparable) getter.invoke(b);
                    if (valA == null) return desc ? 1 : -1;
                    if (valB == null) return desc ? -1 : 1;
                    int cmp = valA.compareTo(valB);
                    return desc ? -cmp : cmp;
                } catch (Exception e) {
                    return 0;
                }
            }).collect(Collectors.toList());
        }

        return lombaList.stream()
                .map(this::mapToLombaResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LombaResponse updateLomba(UUID id, LombaRequest request) {
        Lomba lomba = lombaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lomba tidak ditemukan"));

        // lomba sdh mulai?
        if (lomba.getStatus() != StatusLomba.BELUM_DIMULAI) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tidak dapat mengubah data lomba yang sedang berlangsung atau selesai");
        }

        // lomba sudah h-1 atau belum
        LocalDate today = LocalDate.now();
        LocalDate dDay = lomba.getWaktuTanggal().toLocalDate();
        if (!today.isBefore(dDay)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gagal update karena sudah hari lomba");
        }

        boolean hasPeserta = lomba.getListGantangan().stream()
            .anyMatch(g -> g.getPeserta() != null || g.getStatus() != GantanganStatus.AVAILABLE);
        if (hasPeserta) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gagal update karena sudah ada peserta yang mendaftar di lomba ini");
        }

        if (request.getJumlahJuri() < lomba.getListJuri().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jumlah juri tidak boleh lebih sedikit dari juri yang sudah di-assign");
        }

        lomba.setNamaLomba(request.getNamaLomba());
        lomba.setLokasi(request.getLokasi());
        lomba.setWaktuTanggal(request.getWaktuTanggal());
        lomba.setJenisBurung(request.getJenisBurung());
        lomba.setKelas(request.getKelas());
        lomba.setHargaTiket(request.getHargaTiket());
        lomba.setHadiah(request.getHadiah());
        lomba.setJumlahJuara(request.getHadiah() != null ? request.getHadiah().size() : 0);
        lomba.setJumlahJuri(request.getJumlahJuri());
        lomba.setContactPerson(request.getContactPerson());
        lomba.setDeskripsi(request.getDeskripsi());

        Lomba updatedLomba = lombaRepository.save(lomba);
        return mapToLombaResponse(updatedLomba);
    }

    @Override
    public LombaResponse assignJuriToLomba(UUID lombaId, AssignJuriRequest request) {
        Lomba lomba = lombaRepository.findById(lombaId)
                .orElseThrow(() -> new RuntimeException("Lomba tidak ditemukan"));

        if (request.getJuriIds().size() > lomba.getJumlahJuri()) {
            throw new RuntimeException("Jumlah juri melebihi kapasitas lomba (max: " + lomba.getJumlahJuri() + ")");
        }

        List<User> juriList = new ArrayList<>();
        for (Long juriId : request.getJuriIds()) {
            User juri = userRepository.findById(juriId)
                    .orElseThrow(() -> new RuntimeException("User dengan ID " + juriId + " tidak ditemukan"));

            if (juri.getRole() != Role.JURI) {
                throw new RuntimeException("User " + juri.getUsername() + " bukan JURI");
            }

            // if (lomba.getListJuri().contains(juri)) {
            //     throw new RuntimeException("Juri " + juri.getFullName() + " sudah di-assign ke lomba ini");
            // }

            juriList.add(juri);
        }

        lomba.getListJuri().addAll(juriList);
        Lomba savedLomba = lombaRepository.save(lomba);

        return mapToLombaResponse(savedLomba);
    }

    @Override
    public LombaResponse removeJuriFromLomba(UUID lombaId, Long juriId) {
        Lomba lomba = lombaRepository.findById(lombaId)
                .orElseThrow(() -> new RuntimeException("Lomba tidak ditemukan"));

        User juri = userRepository.findById(juriId)
                .orElseThrow(() -> new RuntimeException("Juri tidak ditemukan"));

        boolean removed = lomba.getListJuri().removeIf(u -> u.getId().equals(juriId));
        if (!removed) {
            throw new RuntimeException("Juri tidak terdaftar di lomba ini");
        }

        Lomba savedLomba = lombaRepository.save(lomba);
        return mapToLombaResponse(savedLomba);
    }

    @Override
    public List<UserSummaryResponse> getAvailableJuri() {
        List<User> juriList = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.JURI && u.isEnabled())
                .collect(Collectors.toList());

        return juriList.stream()
                .map(this::mapToUserSummaryResponse)
                .collect(Collectors.toList());
    }

    private LombaResponse mapToLombaResponse(Lomba lomba) {
        LombaResponse response = new LombaResponse();
        response.setId(lomba.getId());
        response.setNamaLomba(lomba.getNamaLomba());
        response.setLokasi(lomba.getLokasi());
        response.setWaktuTanggal(lomba.getWaktuTanggal());
        response.setJenisBurung(lomba.getJenisBurung());
        response.setKelas(lomba.getKelas());
        response.setHargaTiket(lomba.getHargaTiket());
        response.setHadiah(lomba.getHadiah());
        response.setJumlahJuara(lomba.getJumlahJuara());
        response.setJumlahJuri(lomba.getJumlahJuri());
        response.setContactPerson(lomba.getContactPerson());
        response.setStatus(lomba.getStatus());
        response.setDeskripsi(lomba.getDeskripsi());


        if (lomba.getListJuri() != null) {
            List<UserSummaryResponse> juriResponses = lomba.getListJuri().stream()
                    .map(this::mapToUserSummaryResponse)
                    .collect(Collectors.toList());
            response.setListJuri(juriResponses);
        }

        if (lomba.getListGantangan() != null) {
            List<GantanganResponse> gantanganResponses = lomba.getListGantangan().stream()
                    .map(this::mapToGantanganResponse)
                    .collect(Collectors.toList());
            response.setListGantangan(gantanganResponses);
        }

        return response;
    }

    private GantanganResponse mapToGantanganResponse(Gantangan g) {
        GantanganResponse res = new GantanganResponse();
        res.setId(g.getId());
        res.setNomorGantangan(g.getNomorGantangan());
        res.setStatus(g.getStatus() != null ? g.getStatus().name() : "AVAILABLE");
        
        if (g.getPeserta() != null) {
            res.setPeserta(mapToUserSummaryResponse(g.getPeserta()));
        }

        return res;
    }

    private UserSummaryResponse mapToUserSummaryResponse(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void deleteLomba(UUID id) {
        Lomba lomba = lombaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lomba tidak ditemukan"));

        if (hasRegistrants(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lomba sudah memiliki peserta, tidak dapat dihapus");
        }

        // status=DIBATALKAN adalah satu-satunya mekanisme "hapus" (soft delete via enum)
        // @Where(status != 'DIBATALKAN') otomatis menyembunyikannya dari user biasa
        // Admin/koordinator bisa melihatnya via findAllIncludingDeleted() yang bypass @Where
        lomba.setStatus(StatusLomba.DIBATALKAN);
        lombaRepository.save(lomba);
    }

    private boolean hasRegistrants(UUID id) {
        // TODO: Connect to PendaftaranRepository later
        return false;
    }

    @Override
    public LombaDetailResponse getLombaDetail(UUID id, User currentUser) {
        Lomba lomba = lombaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lomba tidak ditemukan"));

        if (currentUser.getRole() == Role.JURI) {
            boolean isAssigned = lomba.getListJuri().stream()
                    .anyMatch(juri -> juri.getId().equals(currentUser.getId()));
            if (!isAssigned) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Detail hanya tersedia untuk juri yang bertugas");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dDay = lomba.getWaktuTanggal();
        
        // H-1: True jika hari ini sebelum tanggal lomba
        boolean isAtMostHMinus1 = now.toLocalDate().isBefore(dDay.toLocalDate());
        boolean isToday = now.toLocalDate().isEqual(dDay.toLocalDate());

        long bookedCount = lomba.getListGantangan().stream()
                .filter(g -> g.getPeserta() != null).count();
        boolean isFull = bookedCount >= 24;

        // Mapping list gantangan untuk dikirim ke FE
        List<GantanganResponse> gantanganResponses = lomba.getListGantangan().stream()
                .map(this::mapToGantanganResponse)
                .collect(Collectors.toList());

        LombaDetailResponse.LombaDetailResponseBuilder builder = LombaDetailResponse.builder()
                .id(lomba.getId())
                .namaLomba(lomba.getNamaLomba())
                .deskripsi(lomba.getDeskripsi() != null ? lomba.getDeskripsi() : "")
                .lokasi(lomba.getLokasi())
                .waktuTanggal(lomba.getWaktuTanggal())
                .jenisBurung(lomba.getJenisBurung())
                .kelas(lomba.getKelas())
                .hargaTiket(lomba.getHargaTiket())
                .hadiah(lomba.getHadiah())
                .jumlahGantangan(24) 
                .listGantangan(gantanganResponses) 
                .contactPerson(lomba.getContactPerson())
                .status(lomba.getStatus())
                .isFullBooked(isFull);

        Role userRole = currentUser.getRole();

        builder.isEditable(userRole == Role.KOORDINATOR_LOMBA && lomba.getStatus() == StatusLomba.BELUM_DIMULAI && isAtMostHMinus1);
        builder.canToggleOngoing(userRole == Role.KOORDINATOR_LOMBA && lomba.getStatus() == StatusLomba.BELUM_DIMULAI);
        
        builder.isReservable(userRole == Role.PESERTA && isAtMostHMinus1);
        
        builder.canStartJudging(userRole == Role.JURI && lomba.getStatus() == StatusLomba.BERLANGSUNG);
        builder.canViewWinner(lomba.getStatus() == StatusLomba.SELESAI);

        if (userRole != Role.PESERTA) {
            builder.jumlahJuri(lomba.getJumlahJuri());
            builder.listJuri(lomba.getListJuri().stream().map(this::mapToUserSummaryResponse).collect(Collectors.toList()));
        }

        return builder.build();
    }

    @Transactional
    public void updateStatus(UUID id, StatusLomba newStatus) {
        Lomba lomba = lombaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lomba tidak ditemukan"));
        
        lomba.setStatus(newStatus);
        lombaRepository.save(lomba); 
    }
}
