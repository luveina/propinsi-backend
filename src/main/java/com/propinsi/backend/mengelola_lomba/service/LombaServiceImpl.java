package com.propinsi.backend.mengelola_lomba.service;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import com.propinsi.backend.mengelola_lomba.repository.GantanganRepository;
import com.propinsi.backend.mengelola_lomba.repository.LombaRepository;
import com.propinsi.backend.mengelola_lomba.restdto.request.AssignJuriRequest;
import com.propinsi.backend.mengelola_lomba.restdto.request.LombaRequest;
import com.propinsi.backend.mengelola_lomba.restdto.response.GantanganResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.LombaResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.UserSummaryResponse;
import com.propinsi.backend.model.Role;
import com.propinsi.backend.model.User;
import com.propinsi.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        lomba.setJumlahJuri(request.getJumlahJuri());
        lomba.setContactPerson(request.getContactPerson());
        lomba.setStatus(StatusLomba.BELUM_DIMULAI);

        Lomba savedLomba = lombaRepository.save(lomba);

        List<Gantangan> listGantangan = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            Gantangan gantangan = new Gantangan();
            gantangan.setNomorGantangan(i);
            gantangan.setIsAvailable(true);
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
        return lombaRepository.findAll().stream()
                .map(this::mapToLombaResponse)
                .collect(Collectors.toList());
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
            
            if (lomba.getListJuri().contains(juri)) {
                throw new RuntimeException("Juri " + juri.getFullName() + " sudah di-assign ke lomba ini");
            }
            
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
                .filter(u -> u.getRole() == Role.JURI && !u.isDeleted())
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
        response.setJumlahJuri(lomba.getJumlahJuri());
        response.setContactPerson(lomba.getContactPerson());
        response.setStatus(lomba.getStatus());

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
        res.setIsAvailable(g.getIsAvailable());
        
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
}
