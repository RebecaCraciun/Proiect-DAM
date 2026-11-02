package ro.dam.salafitness.service.audit.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.model.entity.AuditLog;
import ro.dam.salafitness.domain.repository.AuditLogRepository;
import ro.dam.salafitness.service.audit.AuditService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    private final Clock clock;

    public AuditServiceImpl(AuditLogRepository auditLogRepository, Clock clock) {
        this.auditLogRepository = auditLogRepository;
        this.clock = clock;
    }

    @Override
    public AuditLog inregistreaza(String actiune, String entitate, Object idEntitate, Integer idUtilizator,
                                  String detalii) {
        AuditLog intrare = new AuditLog(LocalDateTime.now(clock), actiune, entitate, id(idEntitate), idUtilizator,
                detalii);
        log.info("AUDIT {} {}#{} de utilizatorul {}: {}", actiune, entitate, idEntitate, idUtilizator, detalii);
        return auditLogRepository.save(intrare);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> istoricEntitate(String entitate, Object idEntitate) {
        return auditLogRepository.findByEntitateAndIdEntitateOrderByDataOraAsc(entitate, id(idEntitate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> istoricUtilizator(Integer idUtilizator) {
        return auditLogRepository.findByIdUtilizatorOrderByDataOraAsc(idUtilizator);
    }

    private static String id(Object idEntitate) {
        return idEntitate == null ? null : idEntitate.toString();
    }
}
