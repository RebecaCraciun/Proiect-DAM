package ro.dam.salafitness.service.workflow;

import ro.dam.salafitness.domain.model.entity.SedintaPT;
import ro.dam.salafitness.domain.model.enums.StatusSedinta;

import java.time.LocalDateTime;

/**
 * Workflow-urile sedintelor de personal training: UC12, UC16.
 */
public interface SedintaPTWorkflowService {

    /** UC12: RA09 (antrenor), RA13 (fara suprapuneri), sedinta in viitor. */
    SedintaPT programeazaSedinta(Integer idClient, Integer idAntrenor, LocalDateTime dataOra);

    /** UC16: RV17 - statusul final Finalizat sau Absent, doar de antrenorul sedintei, dupa ora programata. */
    SedintaPT finalizeazaSedinta(Long idSedinta, Integer idAntrenor, StatusSedinta statusFinal, String note);

    /** RV17: o sedinta programata se poate anula; ora antrenorului se elibereaza (RA13). */
    SedintaPT anuleazaSedinta(Long idSedinta);
}
