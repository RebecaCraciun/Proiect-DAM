package ro.dam.salafitness.service.workflow;

import ro.dam.salafitness.domain.model.entity.ClasaFitness;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.model.entity.RezervareClasa;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;
import ro.dam.salafitness.service.computation.view.OcupareClasaView;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Workflow-urile agregatului Clasa Fitness: UC08, UC09 (cu UC07 si UC10 incluse), UC11, UC15, UC22.
 */
public interface ClasaFitnessWorkflowService {

    /** UC22: RV14, RA09 (antrenorul titular are rolul "Antrenor"). */
    ClasaFitness definesteClasa(String denumire, Integer idAntrenor, int capacitateMaxima, String descriere,
                                Integer idManager);

    /** UC22: RV15, RA12 (sala nu este ocupata de nicio alta sesiune in acelasi interval). */
    ProgramClasa programeazaSesiune(Integer idClasa, IntervalOrar interval, String salaStudio, Integer idManager);

    /** UC08: sesiunile care incep in [de, pana), cu locurile libere (RC09). */
    List<OcupareClasaView> programClase(LocalDateTime de, LocalDateTime pana);

    /** UC09: RA01, RA02, RA03, RC09. */
    RezervareClasa rezervaLoc(Long idProgram, Integer idClient);

    /** UC11: RA04 */
    RezervareClasa anuleazaRezervare(Long idProgram, Integer idClient);

    /** UC15: RA04 (doar antrenorul titular, doar dupa inceperea sesiunii). */
    RezervareClasa marcheazaPrezenta(Long idProgram, Integer idClient, Integer idAntrenor);
}
