package com.propinsi.backend.mengelola_lomba.service;

import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import com.propinsi.backend.mengelola_lomba.repository.GantanganRepository;
import com.propinsi.backend.mengelola_lomba.repository.LombaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LombaService {

    @Autowired
    private LombaRepository lombaRepository;

    @Autowired
    private GantanganRepository gantanganRepository;

    public Lomba createLomba(Lomba lomba) {
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

        return savedLomba;
    }

    public Lomba getLombaById(UUID id) {
    return lombaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lomba dengan ID " + id + " tidak ditemukan"));
}
    
    public List<Lomba> getAllLomba() {
        return lombaRepository.findAll();
    }
}