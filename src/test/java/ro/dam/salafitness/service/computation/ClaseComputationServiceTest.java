package ro.dam.salafitness.service.computation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;
import ro.dam.salafitness.domain.repository.ClasaFitnessRepository;
import ro.dam.salafitness.service.computation.view.OcupareClasaView;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reguli de calcul pentru clasele de fitness (RC09, V2).
 */
class ClaseComputationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ClaseComputationService claseComputationService;
    @Autowired
    private ClasaFitnessRepository clasaFitnessRepository;

    @Test
    void locuriLibere_RC09_rezervarileAnulateNuOcupaLoc() {
        ClasaFitness clasa = clasaFitnessRepository.save(new ClasaFitness("Pilates", antrenor.getIdUtilizator(), 3, null));
        ProgramClasa sesiune = clasa.programeazaSesiune(IntervalOrar.intre(ACUM.plusDays(1), ACUM.plusDays(1).plusHours(1)), "Studio 1");
        clasaFitnessRepository.flush();
        Long idProgram = sesiune.getIdProgram();
        assertEquals(3, claseComputationService.calculeazaLocuriLibere(sesiune));

        clasa.rezerva(idProgram, 101, ACUM);
        clasa.rezerva(idProgram, 102, ACUM);
        assertEquals(1, claseComputationService.calculeazaLocuriLibere(sesiune));

        clasa.anuleazaRezervare(idProgram, 101, ACUM);
        assertEquals(2, claseComputationService.calculeazaLocuriLibere(sesiune));
    }

    @Test
    void ocupareClase_V2_gradDeOcupare() {
        ClasaFitness clasa = clasaFitnessRepository.save(new ClasaFitness("Spinning", antrenor.getIdUtilizator(), 4, null));
        ProgramClasa sesiune = clasa.programeazaSesiune(IntervalOrar.intre(ACUM.plusDays(1), ACUM.plusDays(1).plusHours(1)), "Sala 2");
        clasaFitnessRepository.flush();
        clasa.rezerva(sesiune.getIdProgram(), 101, ACUM);
        clasaFitnessRepository.flush();

        OcupareClasaView ocupare = claseComputationService.ocupareClase().get(0);

        assertEquals("Spinning", ocupare.clasa());
        assertEquals(1, ocupare.locuriOcupate());
        assertEquals(3, ocupare.locuriLibere());
        assertEquals(new BigDecimal("25.0"), ocupare.gradOcuparePct());
        assertTrue(claseComputationService.ocupareClase(ACUM.plusDays(2), ACUM.plusDays(3)).isEmpty());
    }
}
