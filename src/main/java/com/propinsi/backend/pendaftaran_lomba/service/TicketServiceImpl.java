package com.propinsi.backend.pendaftaran_lomba.service;

import com.propinsi.backend.model.User;
import com.propinsi.backend.pendaftaran_lomba.model.Reservasi;
import com.propinsi.backend.pendaftaran_lomba.model.StatusReservasi;
import com.propinsi.backend.pendaftaran_lomba.repository.ReservasiRepository;
import com.propinsi.backend.pendaftaran_lomba.restdto.response.TicketResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final ReservasiRepository reservasiRepository;

    // Format tanggal: "Mar 28, 2026 - 11.00 WIB"
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy - HH.mm", new Locale("id", "ID"));

    @Override
    public List<TicketResponse> getMyTickets(User currentUser) {
        List<Reservasi> reservasiList =
                reservasiRepository.findByPesertaOrderByWaktuReservasiDesc(currentUser);

        return reservasiList.stream()
                .map(this::toTicketResponse)   // nama method sudah match
                .collect(Collectors.toList());
    }

    // nama method sudah disesuaikan dengan this::toTicketResponse di atas
    private TicketResponse toTicketResponse(Reservasi reservasi) {
        boolean isPaid = reservasi.getStatus() == StatusReservasi.PAID;
        boolean isInvalid = reservasi.getStatus() == StatusReservasi.REJECTED
                || reservasi.getStatus() == StatusReservasi.EXPIRED;

        // can_reupload = false jika sudah 2x ditolak atau status EXPIRED
        boolean canReupload = reservasi.getStatus() != StatusReservasi.EXPIRED
                && (reservasi.getRejectionCount() == null || reservasi.getRejectionCount() < 2);

        // keterangan_tolak hanya untuk status Invalid
        String keteranganTolak = isInvalid ? reservasi.getKeteranganTolak() : null;

        // Nomor gantangan dibutuhkan juga untuk flow upload dari My Tickets.
        Integer nomorGantangan = reservasi.getGantangan() != null
                ? reservasi.getGantangan().getNomorGantangan()
                : null;

        // field di Lomba adalah waktuTanggal, bukan tanggalPelaksanaan
        String tanggal = reservasi.getLomba().getWaktuTanggal() != null
                ? reservasi.getLomba().getWaktuTanggal().format(DATE_FORMATTER) + " WIB"
                : "-";

        // jenisBurung adalah enum JenisBurung — convert ke String yang readable
        String jenisBurung = reservasi.getLomba().getJenisBurung() != null
                ? formatJenisBurung(reservasi.getLomba().getJenisBurung().name())
                : "-";

        Integer blok = isPaid && reservasi.getGantangan() != null
                ? reservasi.getGantangan().getBlok()
                : null;

        return TicketResponse.builder()
                .id(reservasi.getId())
                .namaLomba(reservasi.getLomba().getNamaLomba())
                .tanggal(tanggal)
                .lokasi(reservasi.getLomba().getLokasi())
                .jenisBurung(jenisBurung)
                .kelas(reservasi.getLomba().getKelas())
                .status(mapStatus(reservasi.getStatus()))
                .keteranganTolak(keteranganTolak)
                .canReupload(canReupload)
                .nominal(reservasi.getLomba().getHargaTiket())
                .waktuReservasi(reservasi.getWaktuReservasi())
                .blok(blok)        
                .nomorGantangan(nomorGantangan)
                .build();
    }

    /**
     * Mapping StatusReservasi → label yang dipakai FE
     */
    private String mapStatus(StatusReservasi status) {
        return switch (status) {
            case PAID                -> "Paid";
            case BOOKED              -> "Unpaid";
            case MENUNGGU_KONFIRMASI -> "Menunggu Konfirmasi";
            case REJECTED            -> "Invalid";
            case EXPIRED             -> "Expired";
            default                  -> "Unpaid";
        };
    }

    /**
     * Convert enum name ke format readable
     * contoh: MURAI_BATU → Murai Batu
     */
    private String formatJenisBurung(String enumName) {
        String[] words = enumName.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(" ");
            result.append(word.charAt(0))
                  .append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }
}