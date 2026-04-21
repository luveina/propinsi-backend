package com.propinsi.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class GantanganSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(GantanganSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public GantanganSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureBlokColumnAndBackfill() {
        jdbcTemplate.execute("ALTER TABLE gantangan ADD COLUMN IF NOT EXISTS blok INTEGER");

        int updated = jdbcTemplate.update(
            "UPDATE gantangan SET blok = ((nomor_gantangan - 1) / 6) + 1 WHERE blok IS NULL"
        );

        if (updated > 0) {
            log.info("Backfilled blok for {} gantangan rows", updated);
        }
    }
}
