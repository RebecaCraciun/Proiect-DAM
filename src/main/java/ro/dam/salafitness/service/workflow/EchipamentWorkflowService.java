package ro.dam.salafitness.service.workflow;

import ro.dam.salafitness.domain.model.entity.Echipament;
import ro.dam.salafitness.domain.model.entity.MentenantaEchipament;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Workflow-urile agregatului Echipament: UC24, UC25.
 */
public interface EchipamentWorkflowService {

    /** UC24: Manager sau Tehnician. */
    Echipament inregistreazaEchipament(String denumire, LocalDate dataAchizitie, String stareUzura,
                                       String locatieInSala, Integer idAngajat);

    /** UC24 */
    Echipament mutaEchipament(Integer idEchipament, String locatieNoua, Integer idAngajat);

    /** UC25: RV22, RA10 (responsabilul are rolul "Tehnic"), RB04 (interventia retine tehnicianul). */
    MentenantaEchipament inregistreazaMentenanta(Integer idEchipament, Integer idTehnician, String descriereProblema,
                                                 BigDecimal cost, String stareNoua);
}
