package com.propinsi.backend.pendaftaran_lomba.service;

import com.propinsi.backend.pendaftaran_lomba.model.Reservasi;
import com.propinsi.backend.pendaftaran_lomba.model.StatusReservasi;
import com.propinsi.backend.pendaftaran_lomba.repository.ReservasiGantanganRepository;
import com.propinsi.backend.pendaftaran_lomba.repository.ReservasiRepository;
import com.propinsi.backend.pendaftaran_lomba.restdto.request.BookingRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.request.VerifyRequest;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.BookingResponse;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.DenahResponse;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.ReservasiResponse;
import com.propinsi.backend.repository.UserRepository;
import com.propinsi.backend.mengelola_lomba.model.Gantangan;
import com.propinsi.backend.mengelola_lomba.model.GantanganStatus;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.repository.GantanganRepository;
import com.propinsi.backend.mengelola_lomba.repository.LombaRepository;
import com.propinsi.backend.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@EnableScheduling
public class ReservasiServiceImpl implements ReservasiService  {
    
    @Autowired
    private ReservasiRepository reservasiRepository;

    @Autowired 
    private LombaRepository lombaRepository;

    @Autowired 
    private UserRepository userRepository;
    
    @Autowired
    private GantanganRepository gantanganRepository;

    @Autowired
    private ReservasiGantanganRepository reservasiGantanganRepository;

    @Override
    public List<DenahResponse> getDenah(UUID lombaId) {
        Lomba lomba = lombaRepository.findById(lombaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lomba tidak ditemukan"));

        return lomba.getListGantangan().stream()
                .map(g -> new DenahResponse(g.getNomorGantangan(), g.getStatus().name()))
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse bookSeat(BookingRequest request, String username) {
        User peserta = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        Gantangan gantangan = reservasiGantanganRepository.findByLombaIdAndNomorWithLock(request.getLombaId(), request.getNomorGantangan())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gantangan tidak ditemukan"));

        if (gantangan.getStatus() != GantanganStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nomor gantangan sudah dipesan");
        }

        gantangan.setStatus(GantanganStatus.BOOKED);
        gantangan.setPeserta(peserta);
        gantangan.setBookedAt(nowUtc());
        reservasiGantanganRepository.save(gantangan);

        Reservasi reservasi = new Reservasi();
        reservasi.setPeserta(peserta);
        reservasi.setLomba(gantangan.getLomba());
        reservasi.setGantangan(gantangan);
        reservasi.setWaktuReservasi(nowUtc());
        reservasi.setNominal(gantangan.getLomba().getHargaTiket()); // Set nominal dari harga tiket lomba
        reservasi.setStatus(StatusReservasi.BOOKED);
        reservasi.setRejectionCount(0);
        
        Reservasi savedReservasi = reservasiRepository.save(reservasi);

        return BookingResponse.builder()
                .reservationId(savedReservasi.getId()) 
                .build();
    }

    public List<ReservasiResponse> getAllReservasi() {
        return reservasiRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ReservasiResponse verifyPembayaran(UUID id, VerifyRequest request) {
        Reservasi reservasi = reservasiRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Data reservasi tidak ditemukan"));

        if ("PAID".equalsIgnoreCase(request.getStatus())) {
            reservasi.setStatus(StatusReservasi.PAID);
            
            // Jadikan seat permanen terisi sesuai Acceptance Criteria
            var gantangan = reservasi.getGantangan();
            gantangan.setIsAvailable(false);
            gantangan.setStatus(GantanganStatus.ACTIVE);
            gantangan.setPeserta(reservasi.getPeserta());
            gantanganRepository.save(gantangan);
            
        } else if ("Invalid".equalsIgnoreCase(request.getStatus()) || "REJECTED".equalsIgnoreCase(request.getStatus())) {
            if (request.getKomentar() == null || request.getKomentar().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Komentar wajib diisi saat menolak pembayaran.");
            }
            
            reservasi.setRejectionCount(reservasi.getRejectionCount() + 1);
            reservasi.setKeteranganTolak(request.getKomentar());
            
            if (reservasi.getRejectionCount() >= 2) {
                reservasi.setStatus(StatusReservasi.REJECTED);
                // Lepas seat kembali ke Available
                var gantangan = reservasi.getGantangan();
                gantangan.setIsAvailable(true);
                gantangan.setStatus(GantanganStatus.AVAILABLE);
                gantangan.setPeserta(null);
                gantangan.setBookedAt(null);
                gantanganRepository.save(gantangan);
            } else {
                reservasi.setStatus(StatusReservasi.REJECTED);
                // Beri waktu 2 jam baru sejak ditolak agar peserta bisa upload ulang
                reservasi.setWaktuReservasi(nowUtc());
                
                var gantangan = reservasi.getGantangan();
                gantangan.setBookedAt(nowUtc());
                gantanganRepository.save(gantangan);
            }
        }

        return mapToResponse(reservasiRepository.save(reservasi));
    }



    private ReservasiResponse mapToResponse(Reservasi res) {
        ReservasiResponse dto = new ReservasiResponse();
        dto.setId(res.getId());
        dto.setNamaPeserta(res.getPeserta().getFullName());
        dto.setUsername(res.getPeserta().getUsername());
        dto.setNamaLomba(res.getLomba().getNamaLomba());
        dto.setNomorGantangan(res.getGantangan().getNomorGantangan());
        dto.setNominal(res.getNominal());
        dto.setUrlBukti(res.getUrlBuktiPembayaran());
        dto.setStatus(res.getStatus().name());
        dto.setWaktuReservasi(res.getWaktuReservasi());
        dto.setKeteranganTolak(res.getKeteranganTolak());
        dto.setRejectionCount(res.getRejectionCount());
        return dto;
    }

    @Override
    public ReservasiResponse uploadBuktiPembayaran(UUID reservasiId, MultipartFile file) {
        Reservasi reservasi = reservasiRepository.findById(reservasiId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservasi tidak ditemukan"));

        // LocalDateTime now = LocalDateTime.now();
        // long hoursBetween = ChronoUnit.HOURS.between(reservasi.getWaktuReservasi(), now);

        // if (hoursBetween >= 2) {
        //     // Jika > 2 jam: Ubah seat ke AVAIL, hapus reservasi, return 410
        //     Gantangan gantangan = reservasi.getGantangan();
        //     gantangan.setStatus(GantanganStatus.AVAILABLE);
        //     gantangan.setPeserta(null);
        //     gantangan.setBookedAt(null);
        //     gantanganRepository.save(gantangan);

        //     reservasiRepository.delete(reservasi);
        //     throw new ResponseStatusException(HttpStatus.GONE, "Waktu pembayaran telah habis. Data reservasi dihapus.");
        // }

        if (reservasi.getStatus() == StatusReservasi.EXPIRED) {
        throw new ResponseStatusException(HttpStatus.GONE, "Waktu pembayaran telah habis. Silakan daftar ulang.");
    }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("application/pdf"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format file harus JPG, PNG, atau PDF");
        }

        // Cek ukuran file max 2MB di backend sebagai pengaman tambahan
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ukuran file maksimal 2MB");
        }

try {
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
            Path filePath = uploadPath.resolve(fileName);
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();
            reservasi.setUrlBuktiPembayaran(fileUrl);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Gagal menyimpan file gambar");
        }
        
        reservasi.setStatus(StatusReservasi.MENUNGGU_KONFIRMASI);
        return mapToResponse(reservasiRepository.save(reservasi));
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCancelExpiredReservasi() {
        LocalDateTime now = nowUtc();
        
        List<Reservasi> activeReservations = reservasiRepository.findByStatus(StatusReservasi.BOOKED);

        for (Reservasi reservasi : activeReservations) {
            long hoursBetween = ChronoUnit.HOURS.between(reservasi.getWaktuReservasi(), now);

            if (hoursBetween >= 2) {
                Gantangan gantangan = reservasi.getGantangan();
                if (gantangan != null) {
                    gantangan.setIsAvailable(true);
                    gantangan.setStatus(GantanganStatus.AVAILABLE);
                    gantangan.setPeserta(null);
                    gantangan.setBookedAt(null);
                    gantanganRepository.save(gantangan);
                }

                reservasi.setStatus(StatusReservasi.EXPIRED);
                reservasiRepository.save(reservasi);

                System.out.println("[SCHEDULER] Reservasi ID: " + reservasi.getId() + " otomatis kedaluwarsa (Booked > 2 jam).");
            }
        }
        
        List<Reservasi> rejectedReservations = reservasiRepository.findByStatus(StatusReservasi.REJECTED);

        for (Reservasi reservasi : rejectedReservations) {
            if (reservasi.getRejectionCount() != null && reservasi.getRejectionCount() == 1) {
                long hoursBetween = ChronoUnit.HOURS.between(reservasi.getWaktuReservasi(), now);

                if (hoursBetween >= 24) { 
                    Gantangan gantangan = reservasi.getGantangan();
                    if (gantangan != null) {
                        gantangan.setIsAvailable(true);
                        gantangan.setStatus(GantanganStatus.AVAILABLE);
                        gantangan.setPeserta(null);
                        gantangan.setBookedAt(null);
                        gantanganRepository.save(gantangan);
                    }

                    reservasi.setStatus(StatusReservasi.EXPIRED);
                    reservasiRepository.save(reservasi);

                    System.out.println("[SCHEDULER] Reservasi ID: " + reservasi.getId() + " otomatis EXPIRED (Rejected > 24 jam).");
                }
            }
        }
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
