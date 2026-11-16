package ro.dam.salafitness.domain.model.listener;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import ro.dam.salafitness.config.SalaFitnessConfig;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;

// RB04: scrie in audit_log fiecare creare, modificare si stergere
public class AuditEntitateListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEntitateListener.class);

    static final String SQL_INSERT = "insert into audit_log "
            + "(data_ora, actiune, entitate, id_entitate, id_utilizator, detalii) values (?, ?, ?, ?, ?, ?)";

    public static final String CREARE = "CREARE";
    public static final String MODIFICARE = "MODIFICARE";
    public static final String STERGERE = "STERGERE";

    private JdbcTemplate jdbcTemplate;
    private Clock clock = Clock.system(SalaFitnessConfig.FUS_ORAR);

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired(required = false)
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    // dupa INSERT, ca sa existe id-ul
    @PostPersist
    public void dupaCreare(Object entitate) {
        scrie(CREARE, entitate);
    }

    @PostUpdate
    public void dupaModificare(Object entitate) {
        scrie(MODIFICARE, entitate);
    }

    // inainte de DELETE, cat timp id-ul inca exista
    @PreRemove
    public void inainteDeStergere(Object entitate) {
        scrie(STERGERE, entitate);
    }

    private void scrie(String actiune, Object entitate) {
        if (!(entitate instanceof Auditabil auditabil)) {
            return;
        }
        if (jdbcTemplate == null) {
            log.warn("AuditEntitateListener nu a fost creat de Spring; {} {} nu a fost auditat", actiune,
                    entitate.getClass().getSimpleName());
            return;
        }
        Object id = auditabil.idAudit();
        // JDBC, nu EntityManager: JPA nu permite salvarea altor entitati dintr-un listener
        jdbcTemplate.update(SQL_INSERT,
                Timestamp.valueOf(LocalDateTime.now(clock)),
                actiune,
                entitate.getClass().getSimpleName(),
                id == null ? null : id.toString(),
                null,
                trunchiaza(auditabil.detaliiAudit()));
    }

    private static String trunchiaza(String detalii) {
        return detalii == null || detalii.length() <= 1000 ? detalii : detalii.substring(0, 1000);
    }
}