package com.propinsi.backend.mengelola_lomba.repository;

import com.propinsi.backend.mengelola_lomba.model.JenisBurung;
import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LombaSpecification {

    public static Specification<Lomba> filter(JenisBurung jenisBurung, StatusLomba status) {
        return filter(jenisBurung, status, null, false);
    }

    public static Specification<Lomba> filter(JenisBurung jenisBurung, StatusLomba status, boolean excludeDibatalkan) {
        return filter(jenisBurung, status, null, excludeDibatalkan);
    }

    public static Specification<Lomba> filter(JenisBurung jenisBurung, StatusLomba status, String nama, boolean excludeDibatalkan) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (jenisBurung != null) {
                predicates.add(cb.equal(root.get("jenisBurung"), jenisBurung));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (nama != null && !nama.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("namaLomba")), "%" + nama.toLowerCase() + "%"));
            }

            // Peserta & role lain tidak boleh melihat lomba yang dibatalkan
            if (excludeDibatalkan) {
                predicates.add(cb.notEqual(root.get("status"), StatusLomba.DIBATALKAN));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
