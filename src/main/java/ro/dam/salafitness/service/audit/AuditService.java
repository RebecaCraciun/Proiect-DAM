package ro.dam.salafitness.service.audit;

import ro.dam.salafitness.domain.model.entity.AuditLog;

import java.util.List;

/**
 * Audit al operatiilor de business (RB04 - trasabilitate). Inregistrarea participa la tranzactia
 * apelantului: daca operatia se anuleaza, se anuleaza si intrarea de audit.
 */
public interface AuditService {

    AuditLog inregistreaza(String actiune, String entitate, Object idEntitate, Integer idUtilizator, String detalii);

    List<AuditLog> istoricEntitate(String entitate, Object idEntitate);

    List<AuditLog> istoricUtilizator(Integer idUtilizator);
}
