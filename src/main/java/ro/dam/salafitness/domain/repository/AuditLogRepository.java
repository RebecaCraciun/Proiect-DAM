package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.AuditLog;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntitateAndIdEntitateOrderByDataOraAsc(String entitate, String idEntitate);

    List<AuditLog> findByIdUtilizatorOrderByDataOraAsc(Integer idUtilizator);
}
